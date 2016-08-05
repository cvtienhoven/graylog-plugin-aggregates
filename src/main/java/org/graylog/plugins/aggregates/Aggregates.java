package org.graylog.plugins.aggregates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import org.apache.commons.mail.EmailException;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.rule.alert.RuleAlertSender;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.indexer.results.ResultMessage;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.results.TermsResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.SearchesClusterConfig;
import org.graylog2.indexer.searches.SearchesConfig;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.initializers.IndexerSetupService;
import org.graylog2.plugin.Message;

import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.InvalidRangeParametersException;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.periodical.Periodical;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class Aggregates extends Periodical {
	private int sequence = 0;
	private int maxInterval = 1; // max interval detected in rules

	private final ClusterConfigService clusterConfigService;
	private final Searches searches;
	private final IndexerSetupService indexerSetupService;
	private final RuleService ruleService;
	private final RuleAlertSender alertSender;
	private static final Logger LOG = LoggerFactory.getLogger(Aggregates.class);
	private List<Rule> list;

	@Inject
	public Aggregates(RuleAlertSender alertSender, Searches searches, ClusterConfigService clusterConfigService,
			IndexerSetupService indexerSetupService, RuleService ruleService) {
		LOG.info("constructor");
		this.searches = searches;
		this.clusterConfigService = clusterConfigService;
		this.alertSender = alertSender;
		this.indexerSetupService = indexerSetupService;
		this.ruleService = ruleService;
	}


	@Override
	public void doRun() {
		if (!indexerSetupService.isRunning()) {
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
							
				if (rule.getInterval() > maxInterval) {
					maxInterval = rule.getInterval();
				}
				
				if (sequence % rule.getInterval() == 0) {

					String field = rule.getField();

					List<String> unique_field_list = new ArrayList<String>();
					unique_field_list.add(field);

					int interval_minutes = rule.getInterval();
					int numberOfMatches = rule.getNumberOfMatches();
					boolean matchMoreOrEqual = rule.isMatchMoreOrEqual();

					//TODO: make limit configurable 
					int limit = 100;
					
					String query = rule.getQuery();
					if (rule.getStreamId() != null && rule.getStreamId() != ""){
						query = query + " AND streams:" + rule.getStreamId();
					}

					final TimeRange timeRange = buildRelativeTimeRange(60 * interval_minutes);
					if (null != timeRange) {
						TermsResult result = searches.terms(field, limit, query, /*filter,*/ timeRange);						
						
						
						LOG.info("built query: " + result.getBuiltQuery());
						
						LOG.info("query took " + result.took().format());
						
						Map<String, Long> matchedTerms = new HashMap<String, Long>();
						
						for (Map.Entry<String, Long> term : result.getTerms().entrySet()){
							
							String matchedFieldValue = term.getKey();
							Long count = term.getValue();
							
							if ((matchMoreOrEqual && count >= numberOfMatches)
									|| (!matchMoreOrEqual && count < numberOfMatches)) {

								LOG.info(count + " found for " + field + "=" + matchedFieldValue);
								matchedTerms.put(matchedFieldValue, count);								
							}

						}
						
						if (!matchedTerms.isEmpty()){
							try {
								alertSender.sendEmails(rule, matchedTerms, timeRange);
							} catch (EmailException e) {
								LOG.error("failed to send email: " + e.getMessage());
							} catch (TransportConfigurationException e) {
								LOG.error("failed to send email: " + e.getMessage());
							}
						}

					}

				}

			}
		}

	}

	private TimeRange buildRelativeTimeRange(int range) {
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startOnThisNode() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean stopOnGracefulShutdown() {
		return true;
	}
}
