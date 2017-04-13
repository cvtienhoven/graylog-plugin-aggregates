package org.graylog.plugins.aggregates.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.history.HistoryAggregateItemImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChartFactoryTest {

	
	@Test
	public void testGenerateTimeSeriesChart() throws ParseException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format =  new SimpleDateFormat ("yyyy-MM-dd'T'HH");
		List<HistoryAggregateItem> history = new ArrayList<HistoryAggregateItem>();
		for (int i=0; i<70; i++){
			cal.add(Calendar.DATE, -1);
			
			String day = format.format(cal.getTime());
			history.add(HistoryAggregateItemImpl.create(day, 54+i));
			
		}
		 		
		ChartFactory.generateTimeSeriesChart("test", history, "P1D", cal);
	}
	
	@Test
	public void testGenerateTimeSeriesChartNoHistory() throws ParseException{
		Calendar cal = Calendar.getInstance();
		List<HistoryAggregateItem> history = new ArrayList<HistoryAggregateItem>();		
		 		
		ChartFactory.generateTimeSeriesChart("test", history, "P1D", cal);
	}
}
