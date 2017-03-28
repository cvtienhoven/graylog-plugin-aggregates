package org.graylog.plugins.aggregates.report.schedule;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.AddReportScheduleRequest;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.UpdateReportScheduleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;

import com.mongodb.MongoException;

public interface ReportScheduleService {
    long count();

    ReportSchedule update(String name, ReportSchedule schedule);

    ReportSchedule create(ReportSchedule schedule);
    
    List<ReportSchedule> all();
    
    int destroy(String scheduleName) throws MongoException, UnsupportedEncodingException;

	ReportSchedule fromRequest(AddReportScheduleRequest request);
	
	ReportSchedule fromRequest(UpdateReportScheduleRequest request);
	
	ReportSchedule get(String id);
	
	ReportSchedule updateNextFireTime(String id, Date nextFireTime);

}
