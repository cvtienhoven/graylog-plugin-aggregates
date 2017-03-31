package org.graylog.plugins.aggregates.history;

import java.util.Date;
import java.util.List;

public interface HistoryItemService {
    long count();

    HistoryItem create(HistoryItem historyItem);
    
    List<HistoryItem> all();

	List<HistoryAggregateItem> getForRuleName(String ruleName, int days);
	
	void removeBefore(Date date);

	List<HistoryAggregateItem> getForRuleName(String ruleName, String timespan);

}
