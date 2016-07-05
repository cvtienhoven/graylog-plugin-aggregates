package org.graylog.plugins.aggregates.rule;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.graylog2.database.CollectionName;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
@CollectionName("aggregate_rules")
public abstract class RuleImpl implements Rule{

    @JsonProperty("query")
    @Override
    @NotNull
    public abstract String getQuery();

    @JsonProperty("field")
    @Override
    @NotNull
    public abstract String getField();

    @JsonProperty("numberOfMatches")
    @Override
    @Min(1)
    public abstract int getNumberOfMatches();
    
    @JsonProperty("matchMoreOrEqual")
    @Override
    public abstract boolean isMatchMoreOrEqual();
	
    @JsonProperty("interval")
    @Override
    @Min(1)
    public abstract int getInterval();
		
    @JsonProperty("name")    
    @Override
    @NotNull
    public abstract String getName();
    	
    @JsonProperty("alertReceivers") 
    @Override
    public abstract List<String> getAlertReceivers();
    
    @JsonProperty("enabled")
    @Override
    public abstract boolean isEnabled();
    
	@JsonCreator
    public static RuleImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("query") String query,
                                       @JsonProperty("field") String field,
                                       @JsonProperty("numberOfMatches") int numberOfMatches,
                                       @JsonProperty("matchMoreOrEqual") boolean matchMoreOrEqual,
                                       @JsonProperty("interval") int interval,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("alertReceivers") List<String> alertReceivers,
                                       @JsonProperty("enabled") boolean enabled) {
        return new AutoValue_RuleImpl(query, field, numberOfMatches, matchMoreOrEqual, interval, name, alertReceivers, enabled);
    }
	
	public static RuleImpl create(
            String query,
            String field,
            int numberOfMatches,
            boolean matchMoreOrEqual,
            int interval,
            String name,
            List<String> alertReceivers,
            boolean enabled) {
		return new AutoValue_RuleImpl(query, field, numberOfMatches, matchMoreOrEqual, interval, name, alertReceivers, enabled);
	
	}
}
