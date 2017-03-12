package org.graylog.plugins.aggregates.report.schedule.rest.models.requests;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.graylog.plugins.aggregates.report.schedule.ReportScheduleImpl;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@AutoValue
@JsonAutoDetect
public abstract class AddReportScheduleRequest {
	
    @JsonProperty("reportSchedule")
    @NotNull
    public abstract ReportScheduleImpl getReportSchedule();
    
    @JsonCreator    
    public static AddReportScheduleRequest create(//@JsonProperty("name") @Valid String name,
    		@JsonProperty("reportSchedule") @Valid ReportScheduleImpl reportSchedule

    		) {
        return new AutoValue_AddReportScheduleRequest(reportSchedule);
    }
}
