package org.graylog.plugins.aggregates.maintenance;

import java.util.Calendar;

import javax.inject.Inject;

import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog2.plugin.periodical.Periodical;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AggregatesMaintenance extends Periodical {
	private static final Logger LOG = LoggerFactory.getLogger(AggregatesMaintenance.class);

	private final HistoryItemService historyItemService;
	private final int retentionDays = 31;

	
	@Inject
	public AggregatesMaintenance(HistoryItemService historyItemService) {
		this.historyItemService = historyItemService;
	}

	@Override
	public void doRun() {		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1*retentionDays);
		
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
