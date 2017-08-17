package org.graylog.plugins.aggregates.report;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.apache.commons.mail.EmailException;
import org.drools.core.time.impl.CronExpression;
import org.graylog.plugins.aggregates.history.HistoryAggregateItem;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.periodical.Periodical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatesReport extends Periodical {
	private static final Logger LOG = LoggerFactory.getLogger(AggregatesReport.class);

	private final ReportSender reportSender;
	private final HistoryItemService historyItemService;
	private final RuleService ruleService;
	private final ReportScheduleService reportScheduleService;
	private String hostname = "localhost";

	@Inject
	public AggregatesReport(ReportSender reportSender, HistoryItemService historyItemService, RuleService ruleService,
			ReportScheduleService reportScheduleService) {
		this.reportSender = reportSender;
		this.historyItemService = historyItemService;
		this.ruleService = ruleService;
		this.reportScheduleService = reportScheduleService;

		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			this.hostname = addr.getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	private void setNewFireTime(ReportSchedule reportSchedule, Calendar cal) {
		CronExpression c;
		try {
			c = new CronExpression(reportSchedule.getExpression());
			reportScheduleService.updateNextFireTime(reportSchedule.getId(), c.getNextValidTimeAfter(cal.getTime()));
		} catch (ParseException e) {
			LOG.error("Schedule " + reportSchedule.getName() + " has invalid Cron Expression "
					+ reportSchedule.getExpression());
		}
	}

	private ReportSchedule getMatchingSchedule(Rule rule, List<ReportSchedule> reportSchedules) {
		for (ReportSchedule reportSchedule : reportSchedules) {
			if (rule.getReportSchedules().contains(reportSchedule.getId())) {
				return reportSchedule;
			}
		}
		return null;
	}

	@Override
	public void doRun() {
		Calendar cal = Calendar.getInstance();
		
		String description = "";

		List<ReportSchedule> reportSchedules = reportScheduleService.all();

		List<ReportSchedule> applicableReportSchedules = new ArrayList<ReportSchedule>();

		// get the schedules that apply to the current dateTime
		for (ReportSchedule reportSchedule : reportSchedules) {
			if (reportSchedule.getNextFireTime() == null) {
				setNewFireTime(reportSchedule, cal);
			}

			if (reportSchedule.getNextFireTime() != null && new Date(reportSchedule.getNextFireTime()).before(cal.getTime())) {
				applicableReportSchedules.add(reportSchedule);
				setNewFireTime(reportSchedule, cal);
			}
		}

		// select the rules that match the applicable schedules
		List<Rule> rulesList = ruleService.all();
		Map<String, Map<Rule, List<HistoryAggregateItem>>> receipientsSeries = new HashMap<String, Map<Rule, List<HistoryAggregateItem>>>();
		Map<Rule, ReportSchedule>  ruleScheduleMapping = new HashMap<Rule, ReportSchedule>();
		
		for (Rule rule : rulesList) {
			if (rule.isInReport()) {
				ReportSchedule matchingSchedule = getMatchingSchedule(rule, applicableReportSchedules);
				
				if (matchingSchedule != null) {
					if (matchingSchedule.getReportReceivers() != null && matchingSchedule.getReportReceivers().size() > 0) {
						ruleScheduleMapping.put(rule, matchingSchedule);
						LOG.info("Rule \"" + rule.getName() + "\" will be added to report");
										
						for (String receipient : matchingSchedule.getReportReceivers()) {
							if (!receipientsSeries.containsKey(receipient)) {
								receipientsSeries.put(receipient, new HashMap<Rule, List<HistoryAggregateItem>>());
							}
							receipientsSeries.get(receipient).put(rule,
									historyItemService.getForRuleName(rule.getName(), matchingSchedule.getTimespan()));
						}
					} else {
						LOG.warn("No receivers found in Report Schedule \"" + matchingSchedule.getName() + "\", not using this schedule.");
					}
				
				}

			}
		}

		ByteArrayOutputStream outputStream = null;
		try {
			for (Map.Entry<String, Map<Rule, List<HistoryAggregateItem>>> receipientSeries : receipientsSeries
					.entrySet()) {
				outputStream = new ByteArrayOutputStream();
				
				ReportFactory.createReport(receipientSeries.getValue(), ruleScheduleMapping, cal, outputStream, hostname);
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
