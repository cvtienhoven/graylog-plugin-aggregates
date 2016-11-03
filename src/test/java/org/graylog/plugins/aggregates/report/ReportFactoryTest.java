package org.graylog.plugins.aggregates.report;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.history.HistoryAggregateItemImpl;
import org.graylog.plugins.aggregates.history.HistoryItem;
import org.graylog.plugins.aggregates.history.HistoryItemImpl;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

@RunWith(MockitoJUnitRunner.class)
public class ReportFactoryTest {

	@Test
	public void testGenerateTimeSeriesChart() throws ParseException, FileNotFoundException{
		Map<String, List<HistoryAggregateItem>> map = new HashMap<String, List<HistoryAggregateItem>>();
		
		Calendar cal = Calendar.getInstance();		
		SimpleDateFormat format =  new SimpleDateFormat ("yyyy-MM-dd");
		
		List<HistoryAggregateItem> history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<15; i++){
			cal.add(Calendar.DATE, -1);

			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 10+i));
		}
		
		
		map.put("Rule 1 Test", history);
		
		
		cal = Calendar.getInstance();		
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<21; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 15+i));

		}
		
		map.put("Rule 2", history);
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<30; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));

		}
		
		map.put("Rule 3", history);
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<70; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));
			
		}
		
		map.put("Rule 4", history);
		
		
		history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<100; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));

		}
		
		map.put("Rule 5", history);

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -31);
		
		System.out.println(new ISO8601DateConverter().toString(c.getTime()));
		FileOutputStream outputStream = new FileOutputStream("/tmp/report.pdf"); 
		ReportFactory.createReport(map, 31, outputStream, "test.domain", Calendar.getInstance().getTime());
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
