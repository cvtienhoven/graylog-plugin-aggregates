package org.graylog.plugins.aggregates.report.schedule;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

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
@CollectionName("aggregate_report_schedules")
public abstract class ReportScheduleImpl implements ReportSchedule{
	public static final String FIELD_NAME = "name";
    public static final String FIELD_EXPRESSION = "expression";
	
    @JsonProperty("_id")    
    @Override
    @Nullable
    public abstract String getId();
    
    
	@JsonProperty("name")    
    @Override
    @NotNull
    public abstract String getName();
    
    @JsonProperty("expression")
    @Override
    @NotNull
    public abstract String getExpression();

    @JsonProperty("timespan")
    @Override
    @NotNull
    public abstract String getTimespan();
       
    @JsonProperty("default")
    @Override
    @Nullable
    public abstract boolean isDefaultSchedule();
    
    @JsonProperty("nextFireTime")
    @Override
    @Nullable
    public abstract Long getNextFireTime();

    @JsonProperty("reportReceivers")
    @Override
    @Nullable
    public abstract List<String> getReportReceivers();
    
    
	@JsonCreator
    public static ReportScheduleImpl create(@JsonProperty("_id") String id,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("expression") String expression,
                                       @JsonProperty("timespan") String timespan,
                                       @JsonProperty("default") boolean defaultSchedule,
                                       @JsonProperty("nextFireTime") Long nextFireTime,
                                       @JsonProperty("reportReceivers") List<String> reportReceivers) {
        return new AutoValue_ReportScheduleImpl(id, name, expression, timespan, defaultSchedule, nextFireTime, reportReceivers);
    }
	
}
