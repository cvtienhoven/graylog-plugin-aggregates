
package org.graylog.plugins.aggregates.alert;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.graylog.plugins.aggregates.history.HistoryItem;
import org.graylog.plugins.aggregates.history.HistoryItemImpl;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AbstractAlertCondition;

import org.graylog2.alerts.types.MessageCountAlertCondition;
import org.graylog2.indexer.results.ResultMessage;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.results.TermsResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.SearchesClusterConfig;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.InvalidRangeParametersException;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class AggregatesAlertCondition extends AbstractAlertCondition {
    private static final Logger LOG = LoggerFactory.getLogger(AggregatesAlertCondition.class);
    private final String description;
    private final String query;
    private final String field;
    private final Long numberOfMatches;
    private final boolean matchMoreOrEqual;
    private final Searches searches;
    private final int limit;
    private final int interval;
    private final HistoryItemService historyItemService;
    private final String ruleName;
    private final ClusterConfigService clusterConfigService;

    enum CheckType {
        TERMS("terms count");

        private final String description;

        CheckType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ThresholdType {
        LESS, MORE_OR_EQUAL
    }

    @AssistedInject
    public AggregatesAlertCondition(Searches searches,
                                    ClusterConfigService clusterConfigService,
                                    HistoryItemService historyItemService,
                                    @Assisted Stream stream,
                                    @Nullable @Assisted("id") String id,
                                    @Assisted DateTime createdAt,
                                    @Assisted("userid") String creatorUserId,
                                    @Assisted Map<String, Object> parameters,
                                    @Assisted("title") @Nullable String title) {
        super(stream, id, AggregatesUtil.ALERT_CONDITION_TYPE, createdAt, creatorUserId, parameters, title);

        this.description = (String) parameters.get("description");
        this.query = (String) parameters.get("query");
        this.field = (String) parameters.get("field");
        this.numberOfMatches = (Long)parameters.get("number_of_matches");
        this.matchMoreOrEqual = parameters.get("match_more_or_equal") == null ? true : (boolean) parameters.get("match_more_or_equal");
        this.searches = searches;
        this.limit = 100;
        this.interval = Tools.getNumber(parameters.get("interval"), Integer.valueOf(1)).intValue();
        this.ruleName = (String) parameters.get("rule_name");

        this.clusterConfigService = clusterConfigService;
        this.historyItemService= historyItemService;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CheckResult runCheck() {
        Integer backlogSize = getBacklog();
        boolean backlogEnabled = false;
        int searchLimit = 100;

        if(backlogSize != null && backlogSize > 0) {
            backlogEnabled = true;
            searchLimit = backlogSize;
        }

        List<MessageSummary> summaries = Lists.newArrayListWithCapacity(searchLimit);

        String filter = "streams:" + stream.getId();

        final TimeRange timeRange = buildRelativeTimeRange(60 * this.interval);

        Map<String, Long> matchedTerms = new HashMap<String, Long>();
        TermsResult result = null;

        long ruleCount = 0;
        if (null != timeRange) {
            result = searches.terms(field, limit, query, filter, timeRange);

            LOG.debug("built query: " + result.getBuiltQuery());

            LOG.debug("query took " + result.tookMs() + "ms");

            for (Map.Entry<String, Long> term : result.getTerms().entrySet()) {

                String matchedFieldValue = term.getKey();
                Long count = term.getValue();

                if ((matchMoreOrEqual && count >= numberOfMatches)
                        || (!matchMoreOrEqual && count < numberOfMatches)) {

                    LOG.info(count + " found for " + field + "=" + matchedFieldValue);

                    matchedTerms.put(matchedFieldValue, count);
                    ruleCount += count;

                    if (backlogEnabled) {
                        SearchResult searchResult = searches.search(
                                query + " AND " + field + ": " + matchedFieldValue,
                                filter,
                                timeRange,
                                searchLimit,
                                0,
                                new Sorting(Message.FIELD_TIMESTAMP, Sorting.Direction.DESC)
                        );




                        for (ResultMessage resultMessage : searchResult.getResults()) {
                            if (summaries.size() < searchLimit) {
                                final Message msg = resultMessage.getMessage();
                                summaries.add(new MessageSummary(resultMessage.getIndex(), msg));
                            } else {
                                break;
                            }
                        }
                    } else {

                        summaries = Collections.emptyList();
                    }


                }

            }
        }

        if (result != null && (!matchedTerms.isEmpty() || (result.getTerms().size() == 0 && !matchMoreOrEqual))){
            HistoryItem historyItem = HistoryItemImpl.create(this.ruleName, new Date(), ruleCount);

            historyItemService.create(historyItem);

            LOG.debug("Alert check <{}> found [{}] terms.", id, matchedTerms.size());
            return new CheckResult(true, this, this.description, this.getCreatedAt(), summaries);
        } else {
            return new NegativeCheckResult();
        }

        //return new CheckResult(true, this, this.description, this.getCreatedAt(), null);
    }







    public static class Descriptor extends AlertCondition.Descriptor {
        public Descriptor() {
            super(
                    "Aggregate Rule Alert Condition",
                    "https://github.com/cvtienhoven/graylog-plugin-aggregates",
                    "This condition is triggered when an Aggregates Rule has been satisfied."
            );
        }
    }

    public static class Config implements AlertCondition.Config {
        public Config() {

        }

        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest configurationRequest = ConfigurationRequest.createWithFields();
            configurationRequest.addFields(AbstractAlertCondition.getDefaultConfigurationFields());

            return configurationRequest;
        }
    }

    public interface Factory extends AlertCondition.Factory {
        @Override
        AggregatesAlertCondition create(Stream stream,
                                        @Assisted("id") String id,
                                        DateTime createdAt,
                                        @Assisted("userid") String creatorUserId,
                                        Map<String, Object> parameters,
                                        @Assisted("title") @Nullable String title);

        @Override
        MessageCountAlertCondition.Config config();

        @Override
        MessageCountAlertCondition.Descriptor descriptor();
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

    public boolean parametersEqual(Map<String, Object> parameters){
        if (this.description == null || !this.description.equals((String) parameters.get("description"))){
            return false;
        }
        if (this.query == null || !this.query.equals((String) parameters.get("query"))){
            return false;
        }
        if (this.ruleName == null || !this.ruleName.equals((String) parameters.get("rule_name"))){
            return false;
        }
        if (this.field == null || !this.field.equals((String) parameters.get("field"))){
            return false;
        }
        if (!this.numberOfMatches.equals((Long)parameters.get("number_of_matches"))){
            return false;
        }
        if (this.matchMoreOrEqual != (parameters.get("match_more_or_equal") == null ? true : (boolean) parameters.get("match_more_or_equal"))){
            return false;
        }
        if (this.repeatNotifications != (boolean) parameters.get("repeat_notifications")){
            return false;
        }
        if (this.interval != Tools.getNumber(parameters.get("interval"), Integer.valueOf(1)).intValue()){
            return false;
        }
        return true;
    }

}