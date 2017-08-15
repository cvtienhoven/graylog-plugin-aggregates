package org.graylog.plugins.aggregates.alert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.alarmcallbacks.HTTPAlarmCallback;
import org.graylog2.alerts.AbstractAlertCondition.CheckResult;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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
	
	@InjectMocks
	@Spy
	RuleAlertSender ruleAlertSender;
	
	@Test
	public void testEmailAlarmCallback() throws ParseException, ClassNotFoundException, AlarmCallbackConfigurationException, UnsupportedEncodingException, NotFoundException, AlarmCallbackException{
		AlarmCallbackConfiguration alarmCallbackConfiguration = null;
		when(alarmCallbackConfigurationService.load(Mockito.any(String.class))).thenReturn(alarmCallbackConfiguration);
		
		
		EmailAlarmCallback callback = getMockEmailAlarmCallback();
		Rule rule = getMockRule();
		Map<String, Long> map = new HashMap<String, Long>();
		AggregatesUtil aggregatesUtil = mock(AggregatesUtil.class);
		ruleAlertSender.setAggregatesUtil(aggregatesUtil);
		TimeRange range = AbsoluteRange.create(DateTime.now(), DateTime.now());
		
		when(alarmCallbackFactory.create(alarmCallbackConfiguration)).thenReturn(callback);
		when(aggregatesUtil.buildSummary(rule,configuration,map,range)).thenReturn("");
		
		ruleAlertSender.send(rule, map, range);
		
		verify(aggregatesUtil).buildSummary(rule, configuration, map, range);
		//verify(aggregatesUtil, Mockito.never()).buildSummary(rule, configuration, map, range);
		verify(callback).call(Mockito.any(Stream.class), Mockito.any(CheckResult.class));
		
	}
	
	
	@Test
	public void testHTTPAlarmCallback() throws ParseException, ClassNotFoundException, AlarmCallbackConfigurationException, UnsupportedEncodingException, NotFoundException, AlarmCallbackException{
		AlarmCallbackConfiguration alarmCallbackConfiguration = null;
		when(alarmCallbackConfigurationService.load(Mockito.any(String.class))).thenReturn(alarmCallbackConfiguration);
		HTTPAlarmCallback callback = getMockHTTPAlarmCallback();
		Rule rule = getMockRule();
		AggregatesUtil aggregatesUtil = mock(AggregatesUtil.class);
		ruleAlertSender.setAggregatesUtil(aggregatesUtil);
		Map<String, Long> map = new HashMap<String, Long>();
		TimeRange range = AbsoluteRange.create(DateTime.now(), DateTime.now());
		
		when(alarmCallbackFactory.create(alarmCallbackConfiguration)).thenReturn(callback);
		when(aggregatesUtil.buildSummary(rule,configuration,map,range)).thenReturn("");
		
		ruleAlertSender.send(rule, map, range);
		
		verify(aggregatesUtil).buildSummary(rule, configuration, map, range);
		//verify(aggregatesUtil, Mockito.never()).buildSummaryHTML(rule, configuration, map, range);
		verify(callback).call(Mockito.any(Stream.class), Mockito.any(CheckResult.class));
		
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
		when(rule.getNotificationId()).thenReturn("notificationId");
	
		return rule;
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
