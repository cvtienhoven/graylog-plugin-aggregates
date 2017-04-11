package org.graylog.plugins.aggregates.report.schedule;

public interface ReportSchedule {
	
	public String getId();
	
	public String getName();

	public String getExpression();

	public String getTimespan();
	
	public boolean isDefaultSchedule();
	
	public Long getNextFireTime();
}