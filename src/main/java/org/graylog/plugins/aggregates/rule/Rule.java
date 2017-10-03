package org.graylog.plugins.aggregates.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;

public interface Rule {
	
	public String getQuery();

	public String getField();

	public long getNumberOfMatches();

	public boolean isMatchMoreOrEqual();

	public int getInterval();
	
	public String getName();
	
	public boolean isEnabled();
	
	public String getStreamId();

	public boolean isInReport();

	public List<String> getReportSchedules();

	public String getAlertConditionId();

	public boolean shouldRepeatNotifications();

	public long getBacklog();
}