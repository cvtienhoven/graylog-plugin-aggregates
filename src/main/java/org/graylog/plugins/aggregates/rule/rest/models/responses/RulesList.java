package org.graylog.plugins.aggregates.rule.rest.models.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

import org.graylog.plugins.aggregates.rule.Rule;

@AutoValue
@JsonAutoDetect
public abstract class RulesList {

	@JsonProperty
	public abstract List<Rule> getRules();

	@JsonCreator
	public static RulesList create(@JsonProperty("rules") List<Rule> rules) {
		return new AutoValue_RulesList(rules);
	}

}
