package org.graylog.plugins.aggregates;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;

import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;
import org.graylog2.indexer.results.TermsResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.cluster.Cluster;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;

//  @RunWith(MockitoJUnitRunner.class)
public class AggregatesTest {
	/*
	@Mock
	Searches searches;
	
	@Mock
	ClusterConfigService clusterConfigService;
	
	@Mock
	Cluster cluster;
	
	@Mock
	RuleService ruleService;

	@Mock
	StreamService streamService;

	@Mock
	HistoryItemService historyItemService;
	
	@Mock
	AlertService alertService;
	
	@InjectMocks
	@Spy
	Aggregates aggregates;
	
	@Test
	public void testBuildRelativeTimeRange() {
		
		TimeRange range = aggregates.buildRelativeTimeRange(60);
		assertThat(range, instanceOf(AbsoluteRange.class));
	}
	
	@Test
	public void testDoRunIndexerNotRunning(){
		Aggregates aggregates = mock(Aggregates.class);
		
		when(aggregates.shouldRun()).thenReturn(false);

		
		aggregates.doRun();
		
	}
	
	@Test
	public void testDoRunIndexerRunningNoRules(){
		when(ruleService.all()).thenReturn(new ArrayList<Rule>());
		Mockito.doReturn(true).when(aggregates).shouldRun();
		
		Mockito.doCallRealMethod().when(aggregates).doRun();
		
		aggregates.doRun();
		
		verify(ruleService).all();
		
	}
	
	@Test
	public void testDoRunIndexerRunningOneRule(){		
		Mockito.doReturn(true).when(aggregates).shouldRun();
		
		Mockito.doCallRealMethod().when(aggregates).doRun();
		aggregates.doRun();
		
		verify(ruleService).all();
		
	}
	
	@Test
	public void testDoRunIndexerRunningOneRuleDisabled() throws NotFoundException {
		Mockito.doReturn(true).when(aggregates).shouldRun();
		List<Rule> ruleList = mockRuleList("query","field",1,true,1,"name",new ArrayList<String>(),false,"streamId");
		when(ruleService.all()).thenReturn(ruleList);
		
		Mockito.doCallRealMethod().when(aggregates).doRun();
		aggregates.doRun();
		

		verify(streamService, Mockito.never()).load((ruleList.get(0)).getStreamId());
		
	}
		
	
	@Test
	public void testDoRunIndexerRunningOneRuleEnabledNullTimerange() throws NotFoundException {
		Mockito.doReturn(true).when(aggregates).shouldRun();
		List<Rule> ruleList = mockRuleList("query","field",1,true,1,"name",new ArrayList<String>(),true,"streamId");
		when(ruleService.all()).thenReturn(ruleList);
		Mockito.doReturn(null).when(aggregates).buildRelativeTimeRange(60);

		Mockito.doCallRealMethod().when(aggregates).doRun();
		aggregates.doRun();


		verify(streamService).load(Mockito.anyString());

	}
	

	private TermsResult mockTermsResult(String termsValue, Long termsOccurrences ){
		Map<String,Long> terms = new HashMap<String,Long>();
				
		terms.put(termsValue, termsOccurrences);
		
		TermsResult result = mock(TermsResult.class);
		
		
		when(result.getTerms()).thenReturn(terms);
		when(result.getBuiltQuery()).thenReturn("builtQuery");
		when(result.tookMs()).thenReturn(123L);
		return result;
	}
	
	private List<Rule> mockRuleList(String query,
            String field,
            long numberOfMatches,
            boolean matchMoreOrEqual,
            int interval,
            String name,
            List<String> alertReceivers,
            boolean enabled,
            String streamId) {

		Rule rule = mock(RuleImpl.class);
		when(rule.getQuery()).thenReturn(query);
		when(rule.getField()).thenReturn(field);
		when(rule.getNumberOfMatches()).thenReturn(numberOfMatches);
		when(rule.isMatchMoreOrEqual()).thenReturn(matchMoreOrEqual);
		when(rule.getInterval()).thenReturn(interval);
		when(rule.getName()).thenReturn(name);
		when(rule.isEnabled()).thenReturn(enabled);		
		when(rule.getStreamId()).thenReturn(streamId);
		//when(rule.getNotificationId()).thenReturn(notificationId);
		
		List<Rule> ruleList = new ArrayList<Rule>();
		ruleList.add(rule);

		return ruleList;
	}*/
}
