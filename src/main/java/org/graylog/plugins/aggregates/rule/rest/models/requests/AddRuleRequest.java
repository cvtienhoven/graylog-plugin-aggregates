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
	
    @JsonProperty("rule")
    @NotNull
    public abstract RuleImpl getRule();
    
    @JsonCreator    
    public static AddRuleRequest create(//@JsonProperty("name") @Valid String name,
    		@JsonProperty("rule") @Valid RuleImpl rule

    		) {
        return new AutoValue_AddRuleRequest(rule);
    }
}
