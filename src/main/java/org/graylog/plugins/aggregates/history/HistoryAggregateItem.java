package org.graylog.plugins.aggregates.history;


public interface HistoryAggregateItem {
	public String getMoment();
	
    public long getNumberOfHits();
        
}
