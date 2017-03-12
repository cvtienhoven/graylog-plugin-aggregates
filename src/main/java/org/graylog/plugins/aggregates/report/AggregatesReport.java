package org.graylog.plugins.aggregates.report;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.apache.commons.mail.EmailException;
import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.periodical.Periodical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class AggregatesReport extends Periodical {
	private static final Logger LOG = LoggerFactory.getLogger(AggregatesReport.class);

	private final ReportSender reportSender;
	private final HistoryItemService historyItemService;
	private final RuleService ruleService;
	private String hostname = "localhost";
	
	@Inject
	public AggregatesReport(ReportSender reportSender, HistoryItemService historyItemService, RuleService ruleService) {
		this.reportSender = reportSender;
		this.historyItemService = historyItemService;
		this.ruleService = ruleService;
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			this.hostname = addr.getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void doRun() {
		Calendar cal = Calendar.getInstance();		
		boolean generateReport = false;
		int days = 0;
		String description = "";
		
		
		if (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59) {
			if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
				generateReport = true;
				days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				description = "Monthly";
			}

			if (cal.getActualMaximum(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK)) {
			
				generateReport = true;
				days = cal.getActualMaximum(Calendar.DAY_OF_WEEK);
				description = "Weekly";
			}
		}

		if (generateReport) {
			
			LOG.info("generating " + description + " report");

			List<Rule> rules = ruleService.all();

			Map<String, Map<String, List<HistoryAggregateItem>>> receipientsSeries = new HashMap<String, Map<String, List<HistoryAggregateItem>>>();
			
			/*
			 * Construct a map with key=receipient and value=Map of rule name + series
			 */
			for (Rule rule : rules) {				
				if (rule.isInReport()){
					LOG.info("Rule \"" + rule.getName() + "\" should be added to report");
					for (String receipient: rule.getAlertReceivers()){
						if (!receipientsSeries.containsKey(receipient)){
							receipientsSeries.put(receipient, new HashMap<String, List<HistoryAggregateItem>>());
						}
						receipientsSeries.get(receipient).put(rule.getName(), historyItemService.getForRuleName(rule.getName(), days));						
					}
				}
			}


			ByteArrayOutputStream outputStream = null;
			try {
				for (Map.Entry<String, Map<String, List<HistoryAggregateItem>>> receipientSeries : receipientsSeries.entrySet()){
					outputStream = new ByteArrayOutputStream();
					ReportFactory.createReport(receipientSeries.getValue(), days, outputStream, hostname, cal.getTime());
					byte[] bytes = outputStream.toByteArray();

					reportSender.sendEmail(receipientSeries.getKey(), bytes, description);
				}

			} catch (ParseException e) {
				LOG.error("Failed to create report, " + e.getMessage());
				LOG.debug("Stacktrace: " + e.getStackTrace());
			} catch (TransportConfigurationException e) {

				e.printStackTrace();
			} catch (EmailException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			} finally {
				if (null != outputStream) {
					try {
						outputStream.close();
						outputStream = null;
					} catch (Exception ex) {
					}
				}
			}
			LOG.info("finished generating report");
		}
	}

	@Override
	public int getInitialDelaySeconds() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public int getPeriodSeconds() {
		return 60;
	}

	@Override
	public boolean isDaemon() {
		return true;
	}

	@Override
	public boolean masterOnly() {

		return true;
	}

	@Override
	public boolean runsForever() {
		return false;
	}

	@Override
	public boolean startOnThisNode() {
		return true;
	}

	@Override
	public boolean stopOnGracefulShutdown() {
		return true;
	}

}
