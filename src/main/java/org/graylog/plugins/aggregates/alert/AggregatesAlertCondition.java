
package org.graylog.plugins.aggregates.alert;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.alerts.types.FieldValueAlertCondition;
import org.graylog2.alerts.types.MessageCountAlertCondition;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AggregatesAlertCondition extends AbstractAlertCondition {
    private String description = "Dummy alert to test notifications";


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
    public AggregatesAlertCondition(@Assisted Stream stream,
                                    @Nullable @Assisted("id") String id,
                                    @Assisted DateTime createdAt,
                                    @Assisted("userid") String creatorUserId,
                                    @Assisted Map<String, Object> parameters,
                                    @Assisted("title") @Nullable String title) {
        super(stream, id, AggregatesUtil.ALERT_CONDITION_TYPE, createdAt, creatorUserId, parameters, title);

        this.description = (String) parameters.get("description");

    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CheckResult runCheck() {
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
