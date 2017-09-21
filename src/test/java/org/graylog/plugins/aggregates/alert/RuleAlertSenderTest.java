package org.graylog.plugins.aggregates.alert;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alarmcallbacks.AlarmCallbackHistoryService;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.alarmcallbacks.HTTPAlarmCallback;
import org.graylog2.alerts.AbstractAlertCondition.CheckResult;
import org.graylog2.alerts.AlertConditionFactory;
import org.graylog2.alerts.AlertImpl;
import org.graylog2.alerts.AlertService;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.matchers.Matches;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RuleAlertSenderTest {

	
	@Mock
	EmailConfiguration configuration;
	
	@Mock
	AlarmCallbackConfigurationService alarmCallbackConfigurationService;
	
	@Mock
	AlarmCallbackFactory alarmCallbackFactory;
	
	@Mock
	StreamService streamService;
	
	@Mock
	AlarmCallbackHistoryService alarmCallbackHistoryService;
	
	@Mock
	AlertService alertService;

	@Mock
	RuleService ruleService;

	@Mock
	AlertConditionFactory alertConditionFactory;
	@InjectMocks
	@Spy
	RuleAlertSender ruleAlertSender;
	
	
	@Test
	public void testEmailAlarmCallback() throws ParseException, ClassNotFoundException, AlarmCallbackConfigurationException, UnsupportedEncodingException, NotFoundException, AlarmCallbackException, ValidationException, ConfigurationException {
		/*
		TimeRange range = AbsoluteRange.create(DateTime.now(), DateTime.now());

		Rule rule = getMockRule();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("time", rule.getInterval());

		parameters.put("description", "Aggregates Alert description");
		parameters.put("threshold_type", AggregatesAlertCondition.ThresholdType.HIGHER.toString());
		parameters.put("threshold", rule.getNumberOfMatches());
		parameters.put("grace", 0);
		parameters.put("type", "Aggregates Rule");
		parameters.put("field", rule.getField());
		parameters.put("backlog", 0);

		String title = "Aggregate rule [" + rule.getName() + "] triggered an alert.";

		AlarmCallbackConfiguration alarmCallbackConfiguration = AlarmCallbackConfigurationImpl.create("id", "streamId", "type", "title", new HashMap<String, Object>(), new Date(),"user");
		
		when(alarmCallbackConfigurationService.load(Mockito.any(String.class))).thenReturn(alarmCallbackConfiguration);

		Stream stream = getStream();

		when(streamService.load(Mockito.anyString())).thenReturn(stream);

		AlertCondition alertCondition = getAlertCondition();
		when(alertConditionFactory.createAlertCondition("Aggregates Alert", stream, "", range.getFrom(), "",parameters,title)).thenReturn(alertCondition);

		when(AlertImpl.fromCheckResult(alertCondition.runCheck())).thenReturn(getAlert());

		EmailAlarmCallback callback = getMockEmailAlarmCallback();

		Map<String, Long> map = new HashMap<String, Long>();
		AggregatesUtil aggregatesUtil = mock(AggregatesUtil.class);
		ruleAlertSender.setAggregatesUtil(aggregatesUtil);
		
		when(alarmCallbackFactory.create(alarmCallbackConfiguration)).thenReturn(callback);
		when(aggregatesUtil.buildSummary(rule,configuration,map,range)).thenReturn("");
		
		ruleAlertSender.send(rule, map, range);
		
		verify(aggregatesUtil).buildSummary(rule, configuration, map, range);

		verify(callback).call(Mockito.any(Stream.class), Mockito.any(CheckResult.class));
		*/
	}
	
	
	@Test
	public void testHTTPAlarmCallback() throws ParseException, ClassNotFoundException, AlarmCallbackConfigurationException, UnsupportedEncodingException, NotFoundException, AlarmCallbackException, ValidationException, ConfigurationException {
		/*
		TimeRange range = AbsoluteRange.create(DateTime.now(), DateTime.now());


		Rule rule = getMockRule();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("time", rule.getInterval());
		//parameters.put("rule", rule);
		parameters.put("description", "Aggregates Alert description");
		parameters.put("threshold_type", AggregatesAlertCondition.ThresholdType.HIGHER.toString());
		parameters.put("threshold", rule.getNumberOfMatches());
		parameters.put("grace", 0);
		parameters.put("type", "Aggregates Rule");
		parameters.put("field", rule.getField());
		parameters.put("backlog", 0);

		String title = "Aggregate rule [" + rule.getName() + "] triggered an alert.";


		AlarmCallbackConfiguration alarmCallbackConfiguration = AlarmCallbackConfigurationImpl.create("id", "streamId", "type", "title", new HashMap<String, Object>(), new Date(),"user");
		when(alarmCallbackConfigurationService.load(Mockito.any(String.class))).thenReturn(alarmCallbackConfiguration);
		when(streamService.load(Mockito.anyString())).thenReturn(getStream());

		Stream stream = getStream();

		when(streamService.load(Mockito.anyString())).thenReturn(stream);

		AlertCondition alertCondition = getAlertCondition();
		when(alertConditionFactory.createAlertCondition("Aggregates Alert", stream, "", range.getFrom(), "",parameters,title)).thenReturn(alertCondition);
		Mockito.doReturn(getAlert()).when(ruleAlertSender).getAlert(alertCondition);


		HTTPAlarmCallback callback = getMockHTTPAlarmCallback();
		AggregatesUtil aggregatesUtil = mock(AggregatesUtil.class);
		ruleAlertSender.setAggregatesUtil(aggregatesUtil);
		Map<String, Long> map = new HashMap<String, Long>();

		when(alarmCallbackFactory.create(alarmCallbackConfiguration)).thenReturn(callback);
		when(aggregatesUtil.buildSummary(rule,configuration,map,range)).thenReturn("");


		ruleAlertSender.send(rule, map, range);
		
		verify(aggregatesUtil).buildSummary((Rule)parameters.get("rule"), configuration, map, range);

		verify(callback).call(Mockito.any(Stream.class), Mockito.any(CheckResult.class));
		*/
	}

	private AggregatesAlertCondition getAlertCondition(){
		AggregatesAlertCondition condition = mock(AggregatesAlertCondition.class);
		when(condition.runCheck()).thenReturn(new CheckResult(true,condition,"desription", new DateTime(),null));
		return condition;
	}

	private AlertImpl getAlert(){
		AlertImpl alert = mock(AlertImpl.class);
		return alert;
	}

	private Rule getMockRule(){
		Rule rule = mock(RuleImpl.class);
		when(rule.getQuery()).thenReturn("query");
		when(rule.getField()).thenReturn("field");
		when(rule.getNumberOfMatches()).thenReturn(1L);
		when(rule.isMatchMoreOrEqual()).thenReturn(true);
		when(rule.getInterval()).thenReturn(1);
		when(rule.getName()).thenReturn("name");
		when(rule.isEnabled()).thenReturn(true);		
		when(rule.getStreamId()).thenReturn("streamId");

	
		return rule;
	}
	
	private Stream getStream() {
		Stream stream = new StreamImpl(new HashMap<String, Object>()); 				

		return stream;
	}
	
	private EmailAlarmCallback getMockEmailAlarmCallback(){
		EmailAlarmCallback callback = mock(EmailAlarmCallback.class);
				
		return callback;
	}
	
	private HTTPAlarmCallback getMockHTTPAlarmCallback(){
		HTTPAlarmCallback callback = mock(HTTPAlarmCallback.class);
				
		return callback;
	}
	
}
