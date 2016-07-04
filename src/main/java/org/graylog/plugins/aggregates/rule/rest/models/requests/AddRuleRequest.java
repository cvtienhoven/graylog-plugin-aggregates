package org.graylog.plugins.aggregates.rule.rest.models.requests;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@AutoValue
@JsonAutoDetect
public abstract class AddRuleRequest {
	
	/*
    @JsonProperty("node_id")
    @NotNull
    @Size(min = 1)
    public abstract String nodeId();
*/
    @JsonProperty("rule")
    @NotNull
    public abstract RuleImpl getRule();
    
    @JsonCreator    
    public static AddRuleRequest create(//@JsonProperty("name") @Valid String name,
    		@JsonProperty("rule") @Valid RuleImpl rule
    		/*@JsonProperty("field") @Valid String field,
    		@JsonProperty("numberOfMatches") @Valid int numberOfMatches,
    		@JsonProperty("matchMoreOrEqual") @Valid boolean matchMoreOrEqual,
    		@JsonProperty("interval") @Valid int interval*/    		
    		) {
        return new AutoValue_AddRuleRequest(rule);
    }
}
