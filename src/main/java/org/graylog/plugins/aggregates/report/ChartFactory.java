package org.graylog.plugins.aggregates.report;


import java.awt.Color;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.model.Date;


public class ChartFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ChartFactory.class);
	
	public static JFreeChart generatePieChart(Map<String, Long> totals) {
		DefaultPieDataset dataSet = new DefaultPieDataset();
		
		for (Map.Entry<String, Long> total : totals.entrySet()){
			dataSet.setValue(total.getKey(), total.getValue());
		}		

		JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
				"Totals", dataSet, true, true, false);

		return chart;
	}
	
	
	
	
	public static JFreeChart generateTimeSeriesChart(String title, List<HistoryAggregateItem> history, int days) throws ParseException {

		
		TimeSeries series = new TimeSeries("Aggregate rule hits", Day.class);
		Calendar cal = Calendar.getInstance();
		for (int i=0; i<days; i++){
			cal.add(Calendar.DATE, -1);
			series.add(new Day(cal.getTime()), null);
		}
		
		
		
		for(HistoryAggregateItem historyItem : history){
			Day day = new Day(new Date(historyItem.getDay(),"yyyy-MM-dd"));
			
			LOG.info("day: " + day.getDayOfMonth());			
			series.addOrUpdate(day, historyItem.getNumberOfHits() );
		}
		
		  
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
