package org.graylog.plugins.aggregates.alert;


import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.inject.Inject;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alarmcallbacks.AlarmCallbackHistoryService;
import org.graylog2.alerts.*;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.database.NotFoundException;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.EmbeddedPersistable;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.database.validators.ValidationResult;
import org.graylog2.plugin.database.validators.Validator;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackError;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackResult;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackSuccess;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RuleAlertSender {
	
    protected final EmailConfiguration configuration;
    protected final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    protected final AlarmCallbackFactory alarmCallbackFactory;
    protected final AlarmCallbackHistoryService alarmCallbackHistoryService;
    protected final StreamService streamService;
    protected final AlertService alertService;
    protected final RuleService ruleService;
    protected final AlertConditionFactory alertConditionFactory;

    private static final Logger LOG = LoggerFactory.getLogger(RuleAlertSender.class);
    private AggregatesUtil aggregatesUtil;
    
	@Inject
	public RuleAlertSender(EmailConfiguration configuration, AlarmCallbackConfigurationService alarmCallbackConfigurationService, AlarmCallbackFactory alarmCallbackFactory, StreamService streamService, AlarmCallbackHistoryService alarmCallbackHistoryService, AlertService alertService, RuleService ruleService,
                           AlertConditionFactory alertConditionFactory) {
		this.configuration = configuration;
		this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
		this.alarmCallbackFactory = alarmCallbackFactory;
		this.streamService = streamService;
		this.alarmCallbackHistoryService = alarmCallbackHistoryService;
		this.alertService = alertService;
		this.ruleService = ruleService;
        this.alertConditionFactory = alertConditionFactory;
        setAggregatesUtil(new AggregatesUtil());
	}

	void setAggregatesUtil(AggregatesUtil aggregatesUtil){

	    this.aggregatesUtil = aggregatesUtil;
	}

	public void initialize(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}

	public void send(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) throws NotFoundException, AlarmCallbackConfigurationException, ClassNotFoundException, UnsupportedEncodingException, ValidationException, ConfigurationException {
        if (rule.getCurrentAlertId() == null) {
            LOG.info("No alert active yet, invoking callback and updating rule and alert condition");
	        AlarmCallbackConfiguration alarmCallbackConfiguration = alarmCallbackConfigurationService.load(rule.getNotificationId());
        
            AlarmCallback callback = alarmCallbackFactory.create(alarmCallbackConfiguration);

            Stream triggeredStream = streamService.load(rule.getStreamId());

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("time", rule.getInterval());
            parameters.put("description", AggregatesUtil.getAlertConditionType(rule));
            parameters.put("threshold_type", AggregatesAlertCondition.ThresholdType.HIGHER.toString());
            parameters.put("threshold", rule.getNumberOfMatches());
            parameters.put("grace", 0);
            parameters.put("type", AggregatesUtil.ALERT_CONDITION_TYPE);
            parameters.put("field", rule.getField());
            parameters.put("backlog", 0);

            String title = "Aggregate rule [" + rule.getName() + "] triggered an alert.";

            String description = aggregatesUtil.buildSummary(rule, configuration, matchedTerms, timeRange);



            AggregatesAlertCondition alertCondition = (AggregatesAlertCondition) alertConditionFactory.createAlertCondition(AggregatesUtil.ALERT_CONDITION_TYPE, triggeredStream, null, timeRange.getFrom(), "admin", parameters, title);
            streamService.addAlertCondition(triggeredStream, alertCondition);

            LOG.info("callback to be invoked: " + callback.getName());
            AlarmCallbackResult callbackResult = AlarmCallbackSuccess.create();
        
            try {
                callback.call(streamService.load(rule.getStreamId()), alertCondition.runCheck());
    		} catch (AlarmCallbackException e) {
    			LOG.error("Error while invoking callback " + callback.getName() + ": " + e.getMessage());
    			callbackResult = AlarmCallbackError.create(e.getMessage());
    		}

            String currentAlertId = alertService.save(getAlert(alertCondition));
            ruleService.setCurrentAlertId(rule, currentAlertId);
        } else {
            LOG.info("Alert already active for rule " + rule + ", not invoking callback and not updating current alert with id " + rule.getCurrentAlertId());
        }

    }

    //let's wrap that static method call for testing
    public Alert getAlert(AlertCondition alertCondition){
        return AlertImpl.fromCheckResult(alertCondition.runCheck());
    }

    public void resolveCurrentAlert(Rule rule) throws NotFoundException {
        ruleService.setCurrentAlertId(rule, null);
	    Alert alert = alertService.load(rule.getCurrentAlertId(), rule.getStreamId());
        try {
            Stream stream = streamService.load(rule.getStreamId());
            streamService.removeAlertCondition(stream, alert.getConditionId());
        } catch (Exception e) {
            LOG.error("Failed to remove alert for rule " + rule + ": " +e.getMessage() );
        }
        try {
            alertService.resolveAlert(alert);
            LOG.info("alert resolved for rule " + rule);
        } catch (Exception e){
            LOG.error("Failed to resolve alert for rule " + rule + ": " +e.getMessage());
        }
    }
	
	
}
