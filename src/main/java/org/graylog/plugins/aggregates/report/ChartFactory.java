package org.graylog.plugins.aggregates.report;


import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ChartFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ChartFactory.class);
	private final static int SECONDS_IN_YEAR = 3600*24*366;
	private final static int SECONDS_IN_MONTH = 3600*24*31;
	private final static int SECONDS_IN_DAY = 3600*24;
	private final static int SECONDS_IN_HOUR = 3600;
	
	
	private static TimeSeries initializeSeries(String timespan, Calendar cal, List<HistoryAggregateItem> history) throws ParseException{		
		int seconds = AggregatesUtil.timespanToSeconds(timespan, cal);		
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		TimeSeries series;
		int count = 0;
		
		if (seconds <= SECONDS_IN_DAY) {
			series = new TimeSeries("Aggregate rule hits", Hour.class);
			count = seconds / SECONDS_IN_HOUR;
			for (int i=0; i<count; i++){
				series.add(new Hour(cal.getTime()), null);
				cal.add(Calendar.HOUR, -1);
			}
			for(HistoryAggregateItem historyItem : history){
				Hour hour = new Hour(df.parse(historyItem.getMoment()+":00:00Z"));
				
				series.addOrUpdate(hour, historyItem.getNumberOfHits() );
			}
		} else if (seconds <= SECONDS_IN_MONTH){
			series = new TimeSeries("Aggregate rule hits", Day.class);	
			count = seconds / SECONDS_IN_DAY;
			for (int i=0; i<count; i++){
				series.add(new Day(cal.getTime()), null);
				cal.add(Calendar.DATE, -1);				
			}
			for(HistoryAggregateItem historyItem : history){
				Day day = new Day(df.parse(historyItem.getMoment()+"T00:00:00Z"));
				series.addOrUpdate(day, historyItem.getNumberOfHits() );
			}
		} else if (seconds <= SECONDS_IN_YEAR) {
			series = new TimeSeries("Aggregate rule hits", Month.class);
			count = seconds / SECONDS_IN_MONTH;
			for (int i=0; i<count; i++){
				series.add(new Month(cal.getTime()), null);
				cal.add(Calendar.MONTH, -1);				
			}
			for(HistoryAggregateItem historyItem : history){
				Month month = new Month(df.parse(historyItem.getMoment()+"-01T00:00:00Z"));
				series.addOrUpdate(month, historyItem.getNumberOfHits() );
			}
		} else {
			series = new TimeSeries("Aggregate rule hits", Year.class);
			count = seconds / SECONDS_IN_YEAR;
			for (int i=0; i<count; i++){
				cal.add(Calendar.YEAR, -1);
				series.add(new Year(cal.getTime()), null);
			}
			for(HistoryAggregateItem historyItem : history){
				Year year = new Year(df.parse(historyItem.getMoment()+"-01-01T00:00:00Z"));
				series.addOrUpdate(year, historyItem.getNumberOfHits() );
			}
		}
		
		return series;
	}
	
	public static JFreeChart generateTimeSeriesChart(String title, List<HistoryAggregateItem> history, String timespan, Calendar cal) throws ParseException {
				
		TimeSeries series = initializeSeries(timespan, cal, history);
			  
		TimeSeriesCollection dataset = new TimeSeriesCollection();  
		dataset.addSeries(series);
		IntervalXYDataset idataset = new XYBarDataset(dataset, 1);
		
		
		JFreeChart chart = org.jfree.chart.ChartFactory.createXYBarChart(  
				title, // Title  
				"Date/time",         // X-axis Label
				true,
				"Hits",       // Y-axis Label  
				idataset,        // Dataset
				PlotOrientation.VERTICAL,  
				true,          // Show legend  
				true,          // Use tooltips  
				false          // Generate URLs  
				);
		
		chart.setBackgroundPaint(Color.WHITE);
		chart.setBorderPaint(Color.BLACK);
		
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		
		plot.getRenderer().setSeriesPaint(0, Color.BLUE);
		plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
		chart.removeLegend();

		return chart;
	}
	
}
