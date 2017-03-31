package org.graylog.plugins.aggregates.maintenance;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.plugin.periodical.Periodical;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AggregatesMaintenance extends Periodical {
	private static final Logger LOG = LoggerFactory.getLogger(AggregatesMaintenance.class);

	private final HistoryItemService historyItemService;
	private final ReportScheduleService reportScheduleService;

	private static final int DEFAULT_RETENTION = 31*24*3600; // a month
	
	@Inject
	public AggregatesMaintenance(HistoryItemService historyItemService, ReportScheduleService reportScheduleService) {
		this.historyItemService = historyItemService;
		this.reportScheduleService = reportScheduleService;
	}

	@Override
	public void doRun() {
		Calendar cal = Calendar.getInstance();
		
		List<ReportSchedule> reportSchedules = reportScheduleService.all();
		int retention = DEFAULT_RETENTION;
		for (ReportSchedule reportSchedule : reportSchedules){
			int timespan = AggregatesUtil.timespanToSeconds(reportSchedule.getTimespan(), cal);
			if (timespan > retention){
				retention = timespan;
			}
		}
		
		LOG.debug("Retention is set to " + retention + " seconds (" + new Duration(retention) + ")");
		
		cal.add(Calendar.SECOND, -1*retention);
		
		//remove all items before the current date - retention time
		long initialCount = historyItemService.count();
		historyItemService.removeBefore(cal.getTime());
		LOG.info("removed " + (initialCount-historyItemService.count()) + " history items");
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
