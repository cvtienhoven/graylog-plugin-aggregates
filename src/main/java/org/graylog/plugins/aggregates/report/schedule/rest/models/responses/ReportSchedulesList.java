package org.graylog.plugins.aggregates.report.schedule.rest.models.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;


@AutoValue
@JsonAutoDetect
public abstract class ReportSchedulesList {

	@JsonProperty
	public abstract List<ReportSchedule> getReportSchedules();

	@JsonCreator
	public static ReportSchedulesList create(@JsonProperty("reportSchedules") List<ReportSchedule> reportSchedules) {
		return new AutoValue_ReportSchedulesList(reportSchedules);
	}

}
