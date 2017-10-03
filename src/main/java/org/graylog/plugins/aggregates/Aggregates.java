package org.graylog.plugins.aggregates;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.graylog.plugins.aggregates.alert.AggregatesAlertCondition;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AlertConditionFactory;
import org.graylog2.database.NotFoundException;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.SearchesClusterConfig;
import org.graylog2.indexer.cluster.Cluster;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.InvalidRangeParametersException;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.periodical.Periodical;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class Aggregates extends Periodical {
	private int sequence = 0;
	private int maxInterval = 1; // max interval detected in rules

	private final ClusterConfigService clusterConfigService;
	private final Searches searches;
	private final Cluster cluster;
	private final RuleService ruleService;
	private final HistoryItemService historyItemService;
	private final AlertConditionFactory alertConditionFactory;
	private final StreamService streamService;

	private static final Logger LOG = LoggerFactory.getLogger(Aggregates.class);
	private List<Rule> list;

	@Inject
	public Aggregates(Searches searches, ClusterConfigService clusterConfigService,
					  Cluster cluster, RuleService ruleService, HistoryItemService historyItemService, AlertConditionFactory alertConditionFactory,
					  StreamService streamService) {
		this.searches = searches;
		this.clusterConfigService = clusterConfigService;
		this.cluster = cluster;
		this.ruleService = ruleService;
		this.historyItemService = historyItemService;
		this.alertConditionFactory = alertConditionFactory;
		this.streamService = streamService;
	}

	@VisibleForTesting
	boolean shouldRun(){
		return cluster.isHealthy();
	}


	@Override
	public void doRun() {

		if (!shouldRun()) {
			LOG.warn("Indexer is not running, not checking any rules this run.");
		} else {
			list = ruleService.all();

			if (sequence == maxInterval) {
				sequence = 0;
			}

			sequence++;

			for (Rule rule : list) {
				if (!rule.isEnabled()){
					LOG.debug("Rule '" + rule.getName() + "' is disabled, skipping.");
					continue;
				}


					Stream triggeredStream = null;
					try {
						triggeredStream = streamService.load(rule.getStreamId());
					} catch (NotFoundException e) {
						LOG.error("Stream with ID [{}] not found, skipping rule with name [{}]", rule.getStreamId(), rule.getName());
						continue;
					}


					try {
						streamService.getAlertCondition(triggeredStream, rule.getAlertConditionId());
					} catch (NotFoundException e) {
						LOG.warn("Alert Condition removed for rule [{}], re-instantiating", rule.getName());

						ruleService.update(rule.getName(), ruleService.createAlertConditionForRule(rule));
					}

			}
		}

	}

	@VisibleForTesting
	TimeRange buildRelativeTimeRange(int range) {
		try {
			return restrictTimeRange(RelativeRange.create(range));
		} catch (InvalidRangeParametersException e) {
			LOG.warn("Invalid timerange parameters provided, not executing rule");
			return null;
		}
	}

	protected org.graylog2.plugin.indexer.searches.timeranges.TimeRange restrictTimeRange(
			final org.graylog2.plugin.indexer.searches.timeranges.TimeRange timeRange) {
		final DateTime originalFrom = timeRange.getFrom();
		final DateTime to = timeRange.getTo();
		final DateTime from;

		final SearchesClusterConfig config = clusterConfigService.get(SearchesClusterConfig.class);

		if (config == null || Period.ZERO.equals(config.queryTimeRangeLimit())) {
			from = originalFrom;
		} else {
			final DateTime limitedFrom = to.minus(config.queryTimeRangeLimit());
			from = limitedFrom.isAfter(originalFrom) ? limitedFrom : originalFrom;
		}

		return AbsoluteRange.create(from, to);
	}

	@Override
	public int getInitialDelaySeconds() {
		return 0;
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public int getPeriodSeconds() {
		return 60;
	}

	@Override
	public boolean isDaemon() {
		return true;
	}

	@Override
	public boolean masterOnly() {

		return true;
	}

	@Override
	public boolean runsForever() {
		return false;
	}

	@Override
	public boolean startOnThisNode() {
		return true;
	}

	@Override
	public boolean stopOnGracefulShutdown() {
		return true;
	}
}
