package org.graylog.plugins.aggregates.rule;

import java.util.List;

import org.bson.types.ObjectId;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;

public interface Rule {
	
	public String getQuery();

	public String getField();

	public long getNumberOfMatches();

	public boolean isMatchMoreOrEqual();

	public int getInterval();
	
	public String getName();
	
	public List<String> getAlertReceivers();
	
	public boolean isEnabled();
	
	public String getStreamId();
	
	public boolean isInReport();

	public List<String> getReportSchedules();

	public boolean isSliding();
}