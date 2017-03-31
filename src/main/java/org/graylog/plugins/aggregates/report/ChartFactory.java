package org.graylog.plugins.aggregates.report;


import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import net.fortuna.ical4j.model.Date;


public class ChartFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ChartFactory.class);
	private final static int SECONDS_IN_YEAR = 3600*24*366;
	private final static int SECONDS_IN_MONTH = 3600*24*31;
	private final static int SECONDS_IN_DAY = 3600*24;
	private final static int SECONDS_IN_HOUR = 3600;
	private final static int SECONDS_IN_MINUTE = 3600;
	
	
	private static TimeSeries initializeSeries(String timespan, Calendar cal, List<HistoryAggregateItem> history) throws ParseException{
		java.time.Duration duration = java.time.Duration.parse(timespan);
		int seconds = (int) duration.get(java.time.temporal.ChronoUnit.SECONDS);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		cal.setTimeZone(Calendar.getInstance().getTimeZone());
		
		LOG.info("Timezone: " + Calendar.getInstance().getTimeZone());
		
		TimeSeries series;
		int count = 0;
		
		if (seconds <= SECONDS_IN_DAY) {
			series = new TimeSeries("Aggregate rule hits", Hour.class);
			count = seconds / SECONDS_IN_HOUR;
			for (int i=0; i<count; i++){
				series.add(new Hour(cal.getTime()), null);
				LOG.info("preparing hourly grid for " + cal.getTime());
				cal.add(Calendar.HOUR, -1);
			}
			for(HistoryAggregateItem historyItem : history){
				LOG.info("moment: " + historyItem.getMoment());
				Hour hour = new Hour(df.parse(historyItem.getMoment()+":00:00Z"));
				
				LOG.info("hour: " + hour.getHour());
				series.addOrUpdate(hour, historyItem.getNumberOfHits() );
			}
		} else if (seconds <= SECONDS_IN_MONTH){
			series = new TimeSeries("Aggregate rule hits", Day.class);	
			count = seconds / SECONDS_IN_DAY;
			for (int i=0; i<count; i++){
				cal.add(Calendar.DATE, -1);
				series.add(new Day(cal.getTime()), null);
			}
			for(HistoryAggregateItem historyItem : history){
				LOG.info("moment: " + historyItem.getMoment());
				Day day = new Day(df.parse(historyItem.getMoment()+"T00:00:00Z"));
				
				
				LOG.info("day: " + day.getDayOfMonth());
				series.addOrUpdate(day, historyItem.getNumberOfHits() );
			}
		} else if (seconds <= SECONDS_IN_YEAR) {
			series = new TimeSeries("Aggregate rule hits", Month.class);
			count = seconds / SECONDS_IN_MONTH;
			for (int i=0; i<count; i++){
				cal.add(Calendar.MONTH, -1);
				series.add(new Month(cal.getTime()), null);
			}
			for(HistoryAggregateItem historyItem : history){
				LOG.info("moment: " + historyItem.getMoment());
				Month month = new Month(df.parse(historyItem.getMoment()+"-01T00:00:00Z"));
				
				
				LOG.info("day: " + month.getMonth());
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
				LOG.info("moment: " + historyItem.getMoment());
				Year year = new Year(df.parse(historyItem.getMoment()+"-01-01T00:00:00Z"));
				
				
				LOG.info("day: " + year.getYear());
				series.addOrUpdate(year, historyItem.getNumberOfHits() );
			}
		}
		
		return series;
	}
	
	public static JFreeChart generateTimeSeriesChart(String title, List<HistoryAggregateItem> history, String timespan, Calendar cal) throws ParseException {
		
		//TimeSeries series = new TimeSeries("Aggregate rule hits", Day.class);
		TimeSeries series = initializeSeries(timespan, cal, history);
		
		series.getTimePeriodClass();
		
		
		
		
		  
		TimeSeriesCollection dataset = new TimeSeriesCollection();  
		dataset.addSeries(series);
		IntervalXYDataset idataset = new XYBarDataset(dataset, 1);
		
		
		JFreeChart chart = org.jfree.chart.ChartFactory.createXYBarChart(  
				title, // Title  
				"Date",         // X-axis Label
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
