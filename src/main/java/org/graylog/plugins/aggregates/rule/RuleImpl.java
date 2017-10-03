package org.graylog.plugins.aggregates.rule;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

import org.bson.types.ObjectId;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog2.database.CollectionName;
import org.graylog2.database.PersistedImpl;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("aggregate_rules")
public abstract class RuleImpl implements Rule{


	@JsonProperty("query")
    @Override
    @NotNull
    public abstract String getQuery();

    @JsonProperty("field")
    @Override
    @NotNull
    public abstract String getField();

    @JsonProperty("numberOfMatches")
    @Override
    @Min(1)
    public abstract long getNumberOfMatches();
    
    @JsonProperty("matchMoreOrEqual")
    @Override
    public abstract boolean isMatchMoreOrEqual();
	
    @JsonProperty("interval")
    @Override
    @Min(1)
    public abstract int getInterval();
		
    @JsonProperty("name")    
    @Override
    @NotNull
    public abstract String getName();

    @JsonProperty("enabled")
    @Override
    public abstract boolean isEnabled();
    
    @JsonProperty("streamId")
    @Override
    public abstract String getStreamId();

    @JsonProperty("inReport")
    @Override
    public abstract boolean isInReport();
    
    @JsonProperty("reportSchedules")
    @Override
    @Nullable
    public abstract List<String> getReportSchedules();

    @JsonProperty("alertConditionId")
    @Override
    @Nullable
    public abstract String getAlertConditionId();

    @JsonProperty("repeatNotifications")
    @Override
    @Nullable
    public abstract boolean shouldRepeatNotifications();

    @JsonProperty("backlog")
    @Override
    @Nullable
    @Min(0)
    public abstract long getBacklog();

	@JsonCreator
    public static RuleImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("query") String query,
                                       @JsonProperty("field") String field,
                                       @JsonProperty("numberOfMatches") long numberOfMatches,
                                       @JsonProperty("matchMoreOrEqual") boolean matchMoreOrEqual,
                                       @JsonProperty("interval") int interval,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("enabled") boolean enabled,
                                       @JsonProperty("streamId") String streamId,
                                       @JsonProperty("inReport") boolean inReport,
                                       @JsonProperty("reportSchedules") List<String> reportSchedules,
                                       @JsonProperty("alertConditionId") String alertConditionId,
                                       @JsonProperty("repeatNotifications") boolean repeatNotifications,
                                       @JsonProperty("backlog") long backlog) {
        return new AutoValue_RuleImpl(query, field, numberOfMatches, matchMoreOrEqual, interval, name, enabled, streamId, inReport, reportSchedules, alertConditionId, repeatNotifications, backlog);
    }
	
	public static RuleImpl create(
            String query,
            String field,
            long numberOfMatches,
            boolean matchMoreOrEqual,
            int interval,
            String name,
            boolean enabled,
            String streamId,
            boolean inReport,
            List<String> reportSchedules,
            String alertConditionId,
            boolean repeatNotifications,
            long backlog) {
		return new AutoValue_RuleImpl(query, field, numberOfMatches, matchMoreOrEqual, interval, name, enabled, streamId, inReport, reportSchedules, alertConditionId, repeatNotifications, backlog);
	
	}
}
