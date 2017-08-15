package org.graylog.plugins.aggregates.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

public class AggregatesUtil {

	public static int timespanToSeconds(String timespan, Calendar cal){
		Period period = Period.parse(timespan);
		Duration duration = period.toDurationFrom(new DateTime(cal.getTime()));
		return duration.toStandardSeconds().getSeconds();
	}

	public static String getAlertConditionType(Rule rule){
		String matchDescriptor = rule.getNumberOfMatches() + " or more";
		if (!rule.isMatchMoreOrEqual()){
			matchDescriptor = "less than " + rule.getNumberOfMatches();
		}
		return "The same value of field '" + rule.getField() + "' occurs " + matchDescriptor + " times in a " + rule.getInterval() + " minute interval";
	}
	
	public String buildSummary(Rule rule, EmailConfiguration emailConfiguration, Map<String, Long> matchedTerms, TimeRange timeRange) throws UnsupportedEncodingException {

        final StringBuilder sb = new StringBuilder();

        sb.append("Matched values for field [ " + rule.getField() + " ]\n\n");

        int nameLength = 4;
        int occurrencesLength = 11;

        for (Map.Entry<String, Long> entry : matchedTerms.entrySet()) {
            if (entry.getKey().length() > nameLength) {
                nameLength = entry.getKey().length();
            }
            if (entry.getValue().toString().length() > occurrencesLength) {
                occurrencesLength = entry.getValue().toString().length();
            }
        }

        sb.append(String.format("%-" + (nameLength + 4) + "s", "FIELD").replace(' ', '_'));
        sb.append(String.format("%-" + (occurrencesLength + 4) + "s", "#OCCURRENCES").replace(' ', '_'));
        if (!emailConfiguration.isEnabled()) {
            sb.append("\n");
        } else {
            sb.append(String.format("%-" + (occurrencesLength + 4) + "s", "#STREAM URL") + "\n");
        }

        for (Map.Entry<String, Long> entry : matchedTerms.entrySet()) {
            sb.append(String.format("%-" + (nameLength + 4) + "s", entry.getKey()));
            sb.append(String.format("%-" + (occurrencesLength + 4) + "s", entry.getValue()));

            if (!emailConfiguration.isEnabled()) {
                sb.append("\n");
            } else {
                String streamId = rule.getStreamId();
                String search_uri = "";

                if (streamId != null && streamId != "") {
                    search_uri += "/streams/" + streamId;
                }
                search_uri += "/search?rangetype=absolute&fields=message%2Csource%2C" + rule.getField() + "&from=" + timeRange.getFrom() + "&to=" + timeRange.getTo() + "&q=" + URLEncoder.encode(rule.getQuery() + " AND " + rule.getField() + ":\"" + entry.getKey() + "\"", "UTF-8");
                sb.append(emailConfiguration.getWebInterfaceUri() + search_uri);

            }
        }
        return sb.toString();

    }
}
