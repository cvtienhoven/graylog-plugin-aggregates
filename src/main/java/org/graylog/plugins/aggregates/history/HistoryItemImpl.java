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
@CollectionName("aggregate_history")
public abstract class HistoryItemImpl implements HistoryItem{

    @JsonProperty("ruleName")
    @Override
    @NotNull
    public abstract String getRuleName();

    @JsonProperty("timestamp")
    @Override
    @NotNull
    public abstract Date getTimestamp();
    
    @JsonProperty("numberOfHits")
    @Override
    @Min(0)
    public abstract long getNumberOfHits();
    
    
	@JsonCreator
    public static HistoryItemImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("ruleName") String ruleName,
                                       @JsonProperty("timestamp") Date timestamp,                                       
                                       @JsonProperty("numberOfHits") long numberOfHits) {		
        return new AutoValue_HistoryItemImpl(ruleName, timestamp, numberOfHits);
    }
	
	public static HistoryItemImpl create(
            String ruleName,
            Date timestamp,
            long numberOfHits) {
		return new AutoValue_HistoryItemImpl(ruleName, timestamp, numberOfHits);
	
	}
}
