package org.graylog.plugins.aggregates.rule;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;

import com.mongodb.MongoException;

public interface RuleService {
    long count();

    Rule update(String name, Rule rule);

    Rule create(Rule rule);
    
    List<Rule> all();
    
    int destroy(String ruleName) throws MongoException, UnsupportedEncodingException;

	Rule fromRequest(AddRuleRequest request);
	
	Rule fromRequest(UpdateRuleRequest request);

}
