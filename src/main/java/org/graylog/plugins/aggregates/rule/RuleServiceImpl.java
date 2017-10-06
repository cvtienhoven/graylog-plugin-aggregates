package org.graylog.plugins.aggregates.rule;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import org.graylog.plugins.aggregates.alert.AggregatesAlertCondition;
import org.graylog.plugins.aggregates.history.HistoryItem;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AlertConditionFactory;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

public class RuleServiceImpl implements RuleService {

	private final JacksonDBCollection<RuleImpl, String> coll;
	private final Validator validator;
	private final StreamService streamService;
	private static final Logger LOG = LoggerFactory.getLogger(RuleServiceImpl.class);
	private final AlertConditionFactory alertConditionFactory;
	private final HistoryItemService historyItemService;

	@Inject
	public RuleServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
                           Validator validator, StreamService streamService, AlertConditionFactory alertConditionFactory,
                           HistoryItemService historyItemService) {
		this.validator = validator;
		final String collectionName = RuleImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.coll = JacksonDBCollection.wrap(dbCollection, RuleImpl.class, String.class, mapperProvider.get());
		this.coll.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
		this.streamService = streamService;
		this.alertConditionFactory = alertConditionFactory;
		this.historyItemService = historyItemService;
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
				coll.insert(ruleImpl).getSavedObject();
				return createAlertConditionForRule(rule);
			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + rule.getClass() + ")!");
	}
	
	@Override
	public Rule update(String name, Rule rule) {		
		
		if (rule instanceof RuleImpl) {
			final RuleImpl ruleImpl = (RuleImpl) rule;
			LOG.debug("updated rule: " + ruleImpl);

			if (rule.getAlertConditionId() != null) {
				Stream triggeredStream = null;
				try {
					triggeredStream = streamService.load(rule.getStreamId());
				} catch (NotFoundException e) {
					LOG.error("Stream with ID [{}] not found, skipping update of alert condition", rule.getStreamId(), rule.getName());
				}


				if (triggeredStream != null) {
					Map<String, Object> parameters = AggregatesUtil.parametersFromRule(rule);

					String title = AggregatesUtil.alertConditionTitleFromRule(rule);

					AggregatesAlertCondition alertCondition;
					try {
						alertCondition = (AggregatesAlertCondition) streamService.getAlertCondition(triggeredStream, rule.getAlertConditionId());

						if (!alertCondition.parametersEqual(parameters)) {
                            LOG.info("Parameters of Alert Condition changed for rule [{}], updating Alert Condition", name);
                            createAlertConditionForRule(rule);
						}
					} catch (NotFoundException e) {
						LOG.warn("Alert Condition removed for rule [{}], re-instantiating", rule.getName());
                        createAlertConditionForRule(rule);
					}
				}
			}
			final Set<ConstraintViolation<RuleImpl>> violations = validator.validate(ruleImpl);
			if (violations.isEmpty()) {

				Rule newRule = coll.findAndModify(DBQuery.is("name", name), new BasicDBObject(), new BasicDBObject(),
						false, ruleImpl, true, false);
                if (!name.equals(rule.getName())) {
                    historyItemService.updateHistoryRuleName(name, rule.getName());
                }
                return newRule;

			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}



		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + rule.getClass() + ")!");
	}

	public Rule setAlertConditionId(Rule rule, String alertConditionId){
		if (rule instanceof RuleImpl) {
			final RuleImpl ruleImpl = (RuleImpl) rule;
			LOG.debug("set alertConditionId [{}] for rule [{}] ", alertConditionId, ruleImpl);

			final Set<ConstraintViolation<RuleImpl>> violations = validator.validate(ruleImpl);
			if (violations.isEmpty()) {
                Rule newRule = coll.findAndModify(DBQuery.is("name", ruleImpl.getName()), new BasicDBObject(), new BasicDBObject(), false, DBUpdate.set("alertConditionId", alertConditionId), true, false);
                LOG.debug("Rule after insertion: [{}]", newRule );
                return newRule;
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
		Rule rule =  RuleImpl.create(
				request.getRule().getQuery(), 
				request.getRule().getField(),
				request.getRule().getNumberOfMatches(), 
				request.getRule().isMatchMoreOrEqual(),
				request.getRule().getInterval(), 
				request.getRule().getName(),
				request.getRule().isEnabled(),
				request.getRule().getStreamId(),
				request.getRule().isInReport(),
				request.getRule().getReportSchedules(),
				null,
				request.getRule().shouldRepeatNotifications(),
				request.getRule().getBacklog());


        return rule;

	}

	@Override
	public Rule fromRequest(UpdateRuleRequest request) {
	    Rule rule = RuleImpl.create(
				request.getRule().getQuery(), 
				request.getRule().getField(),
				request.getRule().getNumberOfMatches(), 
				request.getRule().isMatchMoreOrEqual(),
				request.getRule().getInterval(), 
				request.getRule().getName(),
				request.getRule().isEnabled(),
				request.getRule().getStreamId(),
				request.getRule().isInReport(),
				request.getRule().getReportSchedules(),
				request.getRule().getAlertConditionId(),
				request.getRule().shouldRepeatNotifications(),
				request.getRule().getBacklog());


           return rule;
	}

	public Rule createAlertConditionForRule(Rule rule){
        String query = rule.getQuery();
        String streamId = rule.getStreamId();

        if (streamId != null && streamId != ""){
            query = query + " AND streams:" + streamId;
        }

        Map<String, Object> parameters = AggregatesUtil.parametersFromRule(rule);

        String title = AggregatesUtil.alertConditionTitleFromRule(rule);

        Stream triggeredStream = null;
        try {
            triggeredStream = streamService.load(rule.getStreamId());
        } catch (NotFoundException e) {
            LOG.error("Stream with ID [{}] not found", rule.getStreamId());

        }

        AggregatesAlertCondition alertCondition;

        String alertConditionId = null;
        if (rule.getAlertConditionId() != null){
            alertConditionId = rule.getAlertConditionId();
        }

        try {
            alertCondition = (AggregatesAlertCondition) alertConditionFactory.createAlertCondition(AggregatesUtil.ALERT_CONDITION_TYPE, triggeredStream, alertConditionId, DateTime.now(), "admin", parameters, title);
            if (alertConditionId != null) {
                streamService.updateAlertCondition(triggeredStream, alertCondition);
            } else {
                streamService.addAlertCondition(triggeredStream, alertCondition);
            }
            rule = (RuleImpl) setAlertConditionId(rule, alertCondition.getId());
        } catch (ConfigurationException|ValidationException e) {
            LOG.error("Failed to save Alert Condition for rule [{}] ", rule.getName());
        }

        return rule;
    }

	@Override
	public int destroy(String ruleName) {
	    Rule rule = coll.findOne(DBQuery.is("name", ruleName));
        Stream triggeredStream = null;
        try {
            triggeredStream = streamService.load(rule.getStreamId());
            streamService.removeAlertCondition(triggeredStream,rule.getAlertConditionId());
        } catch (NotFoundException e) {
            LOG.error("Stream with ID [{}] not found", rule.getStreamId());
        }

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
