package org.graylog.plugins.aggregates.alert;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alarmcallbacks.AlarmCallbackHistory;
import org.graylog2.alarmcallbacks.AlarmCallbackHistoryImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackHistoryService;
import org.graylog2.alerts.AlertImpl;
import org.graylog2.alerts.AlertService;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.database.NotFoundException;

import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackError;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackResult;
import org.graylog2.rest.models.alarmcallbacks.AlarmCallbackSuccess;
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
    private static final Logger LOG = LoggerFactory.getLogger(RuleAlertSender.class);
    private AggregatesUtil aggregatesUtil;
    
	@Inject
	public RuleAlertSender(EmailConfiguration configuration, AlarmCallbackConfigurationService alarmCallbackConfigurationService, AlarmCallbackFactory alarmCallbackFactory, StreamService streamService, AlarmCallbackHistoryService alarmCallbackHistoryService, AlertService alertService 
    ) {
		this.configuration = configuration;
		this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
		this.alarmCallbackFactory = alarmCallbackFactory;
		this.streamService = streamService;
		this.alarmCallbackHistoryService = alarmCallbackHistoryService;
		this.alertService = alertService;
		setAggregatesUtil(new AggregatesUtil());
	}

	void setAggregatesUtil(AggregatesUtil aggregatesUtil){
		this.aggregatesUtil = aggregatesUtil;
	}
	
	public void initialize(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}

	public void send(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) throws NotFoundException, AlarmCallbackConfigurationException, ClassNotFoundException, UnsupportedEncodingException, ValidationException {
        AlarmCallbackConfiguration alarmCallbackConfiguration = alarmCallbackConfigurationService.load(rule.getNotificationId());
        
        AlarmCallback callback = alarmCallbackFactory.create(alarmCallbackConfiguration);
        
        Stream triggeredStream = streamService.load(rule.getStreamId());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("time", rule.getInterval());
        String title = "Aggregate rule [" + rule.getName() + "] triggered an alert.";
        
        String description = aggregatesUtil.buildSummary(rule, configuration, matchedTerms, timeRange);
                                        
        AggregatesAlertCondition alertCondition = new AggregatesAlertCondition(rule, description, triggeredStream, "", "", timeRange.getFrom(), "", new HashMap<String, Object>(), title);
        
        LOG.info("callback to be invoked: " + callback.getName());
        AlarmCallbackResult callbackResult = AlarmCallbackSuccess.create(); 
        
        try {
			callback.call(streamService.load(rule.getStreamId()), alertCondition.runCheck());		
		} catch (AlarmCallbackException e) {
			LOG.error("Error while invokingcallback " + callback.getName() + ": " + e.getMessage());			
			callbackResult = AlarmCallbackError.create(e.getMessage());
		}
        
        //AlarmCallbackHistory history = AlarmCallbackHistoryImpl.create(new ObjectId().toHexString(), alarmCallbackConfiguration, AlertImpl.fromCheckResult(alertCondition.runCheck()), alertCondition, callbackResult);
        alertService.save(AlertImpl.fromCheckResult(alertCondition.runCheck()));
        //alarmCallbackHistoryService.save(history);
        
        
        

    }
	
	
}
