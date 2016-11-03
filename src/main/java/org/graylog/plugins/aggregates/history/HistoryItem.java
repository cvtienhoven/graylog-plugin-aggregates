package org.graylog.plugins.aggregates.history;

import java.util.Date;

public interface HistoryItem {
	
	public String getRuleName();

	public Date getTimestamp();
	
	public long getNumberOfHits();
		
}