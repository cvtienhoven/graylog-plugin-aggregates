package org.graylog.plugins.aggregates.history;


public interface HistoryAggregateItem {
	public String getDay();
	
    public long getNumberOfHits();
        
}
