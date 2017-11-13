package org.graylog.plugins.aggregates.config;

import com.google.auto.value.AutoValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@AutoValue
public abstract class AggregatesConfig {

    @JsonProperty("purgeHistory")
    public abstract boolean purgeHistory();

    @JsonProperty("historyRetention")
    public abstract String historyRetention();

    @JsonProperty("resolveOrphanedAlerts")
    public abstract boolean resolveOrphanedAlerts();


    @JsonCreator
    public static AggregatesConfig create(@JsonProperty("purgeHistory") boolean purgeHistory,
                                          @JsonProperty("historyRetention") String historyRetention,
                                          @JsonProperty("resolveOrphanedAlerts") boolean resolveOrphanedAlerts) {
        return builder()
                .purgeHistory(purgeHistory)
                .historyRetention(historyRetention)
                .resolveOrphanedAlerts(resolveOrphanedAlerts)
                .build();
    }

    public static AggregatesConfig defaultConfig() {
        return builder()
                .purgeHistory(true)
                .historyRetention("P1M")
                .resolveOrphanedAlerts(false)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AggregatesConfig.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder purgeHistory(boolean purgeHistory);
        public abstract Builder historyRetention(String historyRetention);
        public abstract Builder resolveOrphanedAlerts(boolean resolveOrphanedAlerts);

        public abstract AggregatesConfig build();
    }
}