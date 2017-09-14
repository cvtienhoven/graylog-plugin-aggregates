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

import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
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

    @JsonProperty("alertReceivers")
    @Override
    @Nullable
    public abstract List<String> getAlertReceivers();

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

    @JsonProperty("notificationId")
    @Override
    @Nullable
    public abstract String getNotificationId();

    @JsonProperty("inReport")
    @Override
    public abstract boolean isInReport();
    
    @JsonProperty("reportSchedules")
    @Override
    @Nullable
    public abstract List<String> getReportSchedules();
    
    @JsonProperty("sliding")
    @Override
    @Nullable
    public abstract boolean isSliding();

    @JsonProperty("currentAlertId")
    @Override
    @Nullable
    public abstract String getCurrentAlertId();

	@JsonCreator
    public static RuleImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("query") String query,
                                       @JsonProperty("field") String field,
                                       @JsonProperty("alertReceivers") List<String> alertReceivers,
                                       @JsonProperty("numberOfMatches") long numberOfMatches,
                                       @JsonProperty("matchMoreOrEqual") boolean matchMoreOrEqual,
                                       @JsonProperty("interval") int interval,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("enabled") boolean enabled,
                                       @JsonProperty("streamId") String streamId,
                                       @JsonProperty("notificationId") String notificationId,
                                       @JsonProperty("inReport") boolean inReport,
                                       @JsonProperty("reportSchedules") List<String> reportSchedules,
                                       @JsonProperty("sliding") boolean sliding,
                                       @JsonProperty("currentAlertId") String currentAlertId) {
        return new AutoValue_RuleImpl(query, field, alertReceivers, numberOfMatches, matchMoreOrEqual, interval, name, enabled, streamId, notificationId, inReport, reportSchedules, sliding, currentAlertId);
    }
	
	public static RuleImpl create(
            String query,
            String field,
            List<String> alertReceivers,
            long numberOfMatches,
            boolean matchMoreOrEqual,
            int interval,
            String name,
            boolean enabled,
            String streamId,
            String notificationId,
            boolean inReport,
            List<String> reportSchedules,
            boolean sliding,
            String currentAlertId) {
		return new AutoValue_RuleImpl(query, field, alertReceivers, numberOfMatches, matchMoreOrEqual, interval, name, enabled, streamId, notificationId, inReport, reportSchedules, sliding, currentAlertId);
	
	}
}
