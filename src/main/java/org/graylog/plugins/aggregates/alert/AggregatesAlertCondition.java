
package org.graylog.plugins.aggregates.alert;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.alerts.types.FieldValueAlertCondition;
import org.graylog2.alerts.types.MessageCountAlertCondition;
import org.graylog2.indexer.results.ResultMessage;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class AggregatesAlertCondition extends AbstractAlertCondition {
    private final String description;
    private final String query;
    private final Searches searches;

    enum CheckType {
        MEAN("mean value"), MIN("min value"), MAX("max value"), SUM("sum"), STDDEV("standard deviation");

        private final String description;

        CheckType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    enum ThresholdType {
        LOWER, HIGHER
    }

    @AssistedInject
    public AggregatesAlertCondition(Searches searches,
                                    @Assisted Stream stream,
                                    @Nullable @Assisted("id") String id,
                                    @Assisted DateTime createdAt,
                                    @Assisted("userid") String creatorUserId,
                                    @Assisted Map<String, Object> parameters,
                                    @Assisted("title") @Nullable String title) {
        super(stream, id, AggregatesUtil.ALERT_CONDITION_TYPE, createdAt, creatorUserId, parameters, title);

        this.description = (String) parameters.get("description");
        this.query= (String) parameters.get("query");
        this.searches = searches;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CheckResult runCheck() {
        /*
    }
        String filter = "streams:" + stream.getId();
        //String query = field + ":\"" + value + "\"";
        Integer backlogSize = getBacklog();
        boolean backlogEnabled = false;
        int searchLimit = 1;

        if(backlogSize != null && backlogSize > 0) {
            backlogEnabled = true;
            searchLimit = backlogSize;
        }

        try {
            SearchResult result = searches.search(
                    query,
                    filter,
                    RelativeRange.create(configuration.getAlertCheckInterval()),
                    searchLimit,
                    0,
                    new Sorting(Message.FIELD_TIMESTAMP, Sorting.Direction.DESC)
            );

            final List<MessageSummary> summaries;
            if (backlogEnabled) {
                summaries = Lists.newArrayListWithCapacity(result.getResults().size());
                for (ResultMessage resultMessage : result.getResults()) {
                    final Message msg = resultMessage.getMessage();
                    summaries.add(new MessageSummary(resultMessage.getIndex(), msg));
                }
            } else {
                summaries = Collections.emptyList();
            }

            final long count = result.getTotalResults();

            final String resultDescription = "Stream received messages matching <" + query + "> "
                    + "(Current grace time: " + grace + " minutes)";

            if (count > 0) {
                LOG.debug("Alert check <{}> found [{}] messages.", id, count);
                return new CheckResult(true, this, resultDescription, Tools.nowUTC(), summaries);
            } else {
                LOG.debug("Alert check <{}> returned no results.", id);
                return new NegativeCheckResult();
            }


*/
        return new CheckResult(true, this, this.description, this.getCreatedAt(), null);
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
}
