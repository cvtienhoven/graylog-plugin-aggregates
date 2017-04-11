package org.graylog.plugins.aggregates;


import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import org.apache.commons.mail.EmailException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.common.lucene.Lucene;
import org.graylog.plugins.aggregates.history.HistoryItem;
import org.graylog.plugins.aggregates.history.HistoryItemImpl;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.rule.alert.RuleAlertSender;
import org.graylog2.indexer.results.TermsResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.SearchesClusterConfig;
import org.graylog2.initializers.IndexerSetupService;

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
	private final IndexerSetupService indexerSetupService;
	private final RuleService ruleService;
	private final HistoryItemService historyItemService;
	private final RuleAlertSender alertSender;
	private static final Logger LOG = LoggerFactory.getLogger(Aggregates.class);
	private List<Rule> list;

	@Inject
	public Aggregates(RuleAlertSender alertSender, Searches searches, ClusterConfigService clusterConfigService,
			IndexerSetupService indexerSetupService, RuleService ruleService, HistoryItemService historyItemService) {		
		this.searches = searches;
		this.clusterConfigService = clusterConfigService;
		this.alertSender = alertSender;
		this.indexerSetupService = indexerSetupService;
		this.ruleService = ruleService;
		this.historyItemService = historyItemService;
	}
	
	@VisibleForTesting
	boolean shouldRun(){
	  return indexerSetupService.isRunning();
	  
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
				
				int interval_minutes = rule.getInterval();
				
						
				if (interval_minutes > maxInterval) {
					maxInterval = rule.getInterval();
				}
				
				//always evaluate when isSliding()
				if (rule.isSliding() || sequence % interval_minutes == 0) {
				
					String field = rule.getField();

					List<String> unique_field_list = new ArrayList<String>();
					unique_field_list.add(field);

					long numberOfMatches = rule.getNumberOfMatches();
					boolean matchMoreOrEqual = rule.isMatchMoreOrEqual();

					//TODO: make limit configurable 
					int limit = 100;
					
					String query = rule.getQuery();
					String streamId = rule.getStreamId();
					
					if (streamId != null && streamId != ""){
						query = query + " AND streams:" + streamId;
					}
					
					
					
					
					final TimeRange timeRange = buildRelativeTimeRange(60 * interval_minutes);
					if (null != timeRange) {
						TermsResult result = searches.terms(field, limit, query, /*filter,*/ timeRange);						
						
						
						LOG.debug("built query: " + result.getBuiltQuery());
						
						LOG.debug("query took " + result.took().format());
						
						Map<String, Long> matchedTerms = new HashMap<String, Long>();
						long ruleCount = 0;
						
						for (Map.Entry<String, Long> term : result.getTerms().entrySet()){
							
							String matchedFieldValue = term.getKey();
							Long count = term.getValue();
							
							if ((matchMoreOrEqual && count >= numberOfMatches)
									|| (!matchMoreOrEqual && count < numberOfMatches)) {

								LOG.info(count + " found for " + field + "=" + matchedFieldValue);
								
								matchedTerms.put(matchedFieldValue, count);
								
								ruleCount += count;
							}

						}
						
						if (!matchedTerms.isEmpty()){
							HistoryItem historyItem = HistoryItemImpl.create(rule.getName(), new Date(), ruleCount);
							
							historyItemService.create(historyItem);

							
							try {
								alertSender.sendEmails(rule, matchedTerms, timeRange, Calendar.getInstance().getTime());
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
