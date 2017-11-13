package org.graylog.plugins.aggregates;

import org.graylog.plugins.aggregates.alert.AggregatesAlertCondition;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.history.HistoryItemServiceImpl;
import org.graylog.plugins.aggregates.maintenance.AggregatesMaintenance;
import org.graylog.plugins.aggregates.permissions.RuleRestPermissions;
import org.graylog.plugins.aggregates.permissions.ReportScheduleRestPermissions;
import org.graylog.plugins.aggregates.report.AggregatesReport;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.rule.RuleServiceImpl;
import org.graylog.plugins.aggregates.rule.rest.RuleResource;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleService;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleServiceImpl;
import org.graylog.plugins.aggregates.report.schedule.rest.ReportScheduleResource;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.alerts.AlertService;
import org.graylog2.alerts.AlertServiceImpl;
import org.graylog2.alerts.types.FieldContentValueAlertCondition;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;
import org.graylog2.plugin.alarms.AlertCondition;

import java.util.Collections;
import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class AggregatesModule extends PluginModule {
    /**
     * Returns all configuration beans required by this plugin.
     *
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        bind(AlertService.class).to(AlertServiceImpl.class);
    	bind(RuleService.class).to(RuleServiceImpl.class);
    	bind(ReportScheduleService.class).to(ReportScheduleServiceImpl.class);
    	bind(HistoryItemService.class).to(HistoryItemServiceImpl.class);    	    	    	
    	
        //saddPeriodical(Aggregates.class);
        addPeriodical(AggregatesReport.class);
        addPeriodical(AggregatesMaintenance.class);
        addPermissions(RuleRestPermissions.class);
        addPermissions(ReportScheduleRestPermissions.class);
        addRestResource(RuleResource.class);
        addRestResource(ReportScheduleResource.class);

        addAlertCondition(AggregatesUtil.ALERT_CONDITION_TYPE, AggregatesAlertCondition.class, AggregatesAlertCondition.Factory.class);

        /*
         * Register your plugin types here.
         *
         * Examples:
         *
         * addMessageInput(Class<? extends MessageInput>);
         * addMessageFilter(Class<? extends MessageFilter>);
         * addMessageOutput(Class<? extends MessageOutput>);
         * addPeriodical(Class<? extends Periodical>);
         * addAlarmCallback(Class<? extends AlarmCallback>);
         * addInitializer(Class<? extends Service>);
         * addRestResource(Class<? extends PluginRestResource>);
         *
         *
         * Add all configuration beans returned by getConfigBeans():
         *
         * addConfigBeans();
         */
    }
}
