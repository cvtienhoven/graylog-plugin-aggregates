package org.graylog.plugins.aggregates.rule;

import java.util.List;

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
}