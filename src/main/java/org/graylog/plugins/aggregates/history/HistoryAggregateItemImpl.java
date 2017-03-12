package org.graylog.plugins.aggregates.history;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog2.database.CollectionName;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
public abstract class HistoryAggregateItemImpl implements HistoryAggregateItem{
    
	@JsonProperty("moment")
    @Override
    public abstract String getMoment();
	
    @JsonProperty("numberOfHits")
    @Override
    public abstract long getNumberOfHits();
    
    
	@JsonCreator
    public static HistoryAggregateItemImpl create(@JsonProperty("_id") String objectId,
    								   @JsonProperty("moment") String moment,
                                       @JsonProperty("numberOfHits") long numberOfHits) {		
        return new AutoValue_HistoryAggregateItemImpl(moment, numberOfHits);
    }
	
	public static HistoryAggregateItemImpl create(
			String moment,
            long numberOfHits) {
		return new AutoValue_HistoryAggregateItemImpl(moment, numberOfHits);
	
	}
}
