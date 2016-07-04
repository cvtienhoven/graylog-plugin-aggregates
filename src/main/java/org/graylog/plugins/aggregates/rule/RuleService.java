package org.graylog.plugins.aggregates.rule;

import java.util.List;

import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;

public interface RuleService {
    long count();

    Rule update(String name, Rule rule);

    Rule create(Rule rule);
    
    List<Rule> all();
    
    int destroy(String ruleName);

	Rule fromRequest(AddRuleRequest request);
	
	Rule fromRequest(UpdateRuleRequest request);

}
