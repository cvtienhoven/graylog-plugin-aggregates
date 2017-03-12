package org.graylog.plugins.aggregates.util;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

public class AggregatesUtil {

	public static int timespanToSeconds(String timespan, Calendar cal){
		Period period = Period.parse(timespan);
		Duration duration = period.toDurationFrom(new DateTime(cal.getTime()));
		return duration.toStandardSeconds().getSeconds();
	}
	
}
