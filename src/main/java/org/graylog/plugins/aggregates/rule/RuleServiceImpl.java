package org.graylog.plugins.aggregates.rule;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

public class RuleServiceImpl implements RuleService {

	private final JacksonDBCollection<RuleImpl, String> coll;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(RuleServiceImpl.class);

	@Inject
	public RuleServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
			Validator validator) {
		this.validator = validator;
		final String collectionName = RuleImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.coll = JacksonDBCollection.wrap(dbCollection, RuleImpl.class, String.class, mapperProvider.get());
		this.coll.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
	}

	@Override
	public long count() {
		return coll.count();
	}

	@Override
	public Rule create(Rule rule) {
		if (rule instanceof RuleImpl) {
			final RuleImpl ruleImpl = (RuleImpl) rule;
			final Set<ConstraintViolation<RuleImpl>> violations = validator.validate(ruleImpl);
			if (violations.isEmpty()) {				
				return coll.insert(ruleImpl).getSavedObject();

			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + rule.getClass() + ")!");
	}
	
	@Override
	public Rule update(String name, Rule rule) {
		LOG.info("name: " + name);
		
		if (rule instanceof RuleImpl) {
			final RuleImpl ruleImpl = (RuleImpl) rule;
			LOG.info("rule: " + ruleImpl);
			final Set<ConstraintViolation<RuleImpl>> violations = validator.validate(ruleImpl);
			if (violations.isEmpty()) {
				//return coll.update(DBQuery.is("name", name), ruleImpl, false, false).getSavedObject();
				return coll.findAndModify(DBQuery.is("name", name), new BasicDBObject(), new BasicDBObject(),
						false, ruleImpl, true, false);
			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + rule.getClass() + ")!");
	}

	@Override
	public List<Rule> all() {		
		return toAbstractListType(coll.find());
	}

	@Override
	public Rule fromRequest(AddRuleRequest request) {
		return RuleImpl.create(
				request.getRule().getQuery(), 
				request.getRule().getField(),
				request.getRule().getNumberOfMatches(), 
				request.getRule().isMatchMoreOrEqual(),
				request.getRule().getInterval(), 
				request.getRule().getName(),
				request.getRule().getAlertReceivers());
	}

	@Override
	public Rule fromRequest(UpdateRuleRequest request) {
		return RuleImpl.create(
				request.getRule().getQuery(), 
				request.getRule().getField(),
				request.getRule().getNumberOfMatches(), 
				request.getRule().isMatchMoreOrEqual(),
				request.getRule().getInterval(), 
				request.getRule().getName(),
				request.getRule().getAlertReceivers());
	}
	
	@Override
	public int destroy(String ruleName) {
		return coll.remove(DBQuery.is("name", ruleName)).getN();
	}

	private List<Rule> toAbstractListType(DBCursor<RuleImpl> rules) {
		return toAbstractListType(rules.toArray());
	}

	private List<Rule> toAbstractListType(List<RuleImpl> rules) {
		final List<Rule> result = Lists.newArrayListWithCapacity(rules.size());
		result.addAll(rules);

		return result;
	}
}
