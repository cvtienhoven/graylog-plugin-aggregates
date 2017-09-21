package org.graylog.plugins.aggregates.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.history.HistoryAggregateItemImpl;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleImpl;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

@RunWith(MockitoJUnitRunner.class)
public class ReportFactoryTest {

	@Test
	public void testGenerateTimeSeriesChart() throws ParseException, FileNotFoundException{
		Map<Rule, List<HistoryAggregateItem>> map = new HashMap<Rule, List<HistoryAggregateItem>>();
		
		Calendar cal = Calendar.getInstance();		
		SimpleDateFormat format =  new SimpleDateFormat ("yyyy-MM-dd'T'HH");
		
		List<HistoryAggregateItem> history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<15; i++){
			cal.add(Calendar.DATE, -1);

			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 10+i));
		}
		List<String> reportReceivers = new ArrayList<String>();

		ReportSchedule schedule = ReportScheduleImpl.create("1231231", "name", "expression", "P1D", false, 0L, reportReceivers);


		Rule rule1 = RuleImpl.create("", "query", "field", null, 1L, true, 1, "Rule 1", true, "", true,new ArrayList<String>(), true, null, true);
		map.put(rule1, history);
		
		
		cal = Calendar.getInstance();		
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<21; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 15+i));

		}

		Rule rule2 = RuleImpl.create("", "query", "field", null, 1L, true, 1, "Rule 2", true, "", true,new ArrayList<String>(), true, null, true);
		map.put(rule2, history);
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<30; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));

		}
		
		Rule rule3 = RuleImpl.create("", "query", "field", null, 1L, true, 1, "Rule 3", true, "", true,new ArrayList<String>(), true, null, true);
		map.put(rule3, history);
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<70; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));
			
		}
		
		Rule rule4 = RuleImpl.create("", "query", "field", null, 1L, true, 1, "Rule 4", true, "", true,new ArrayList<String>(), true, null, true);
		map.put(rule4, history);
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<100; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));

		}
		
		Rule rule5 = RuleImpl.create("", "query", "field", null, 1L, true, 1, "Rule 5", true, "", true,new ArrayList<String>(), true, null, true);
		map.put(rule5, history);
		
		Map<Rule, ReportSchedule> mapping = new HashMap<Rule, ReportSchedule>();
		mapping.put(rule1, schedule);
		mapping.put(rule2, schedule);
		mapping.put(rule3, schedule);
		mapping.put(rule4, schedule);
		mapping.put(rule5, schedule);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -31);
		
		System.out.println(new ISO8601DateConverter().toString(c.getTime()));
		
		FileOutputStream outputStream = new FileOutputStream("/tmp/report.pdf"); 
		ReportFactory.createReport(map, mapping, Calendar.getInstance(), outputStream, "test.domain");
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
