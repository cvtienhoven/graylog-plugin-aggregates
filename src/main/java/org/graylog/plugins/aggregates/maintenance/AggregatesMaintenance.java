package org.graylog.plugins.aggregates.maintenance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.graylog.plugins.aggregates.config.AggregatesConfig;
import org.graylog.plugins.aggregates.history.HistoryItemService;
import org.graylog.plugins.aggregates.report.schedule.ReportSchedule;
import org.graylog.plugins.aggregates.report.schedule.ReportScheduleService;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.rule.RuleService;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.cluster.ClusterConfigChangedEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.periodical.Periodical;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AggregatesMaintenance extends Periodical {
    private static final Logger LOG = LoggerFactory.getLogger(AggregatesMaintenance.class);
    private final AlertService alertService;
    private final StreamService streamService;
    private final RuleService ruleService;
    private final HistoryItemService historyItemService;
    private final ReportScheduleService reportScheduleService;
    private final ClusterConfigService clusterConfigService;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<AggregatesConfig> config;

    private static final int DEFAULT_RETENTION = 31 * 24 * 3600; // a month

    @Inject
    public AggregatesMaintenance(AlertService alertService, RuleService ruleService, StreamService streamService, HistoryItemService historyItemService, ReportScheduleService reportScheduleService, @Named("daemonScheduler") ScheduledExecutorService scheduler, ClusterConfigService clusterConfigService, EventBus eventBus) {
        this.alertService = alertService;
        this.ruleService = ruleService;
        this.streamService = streamService;
        this.historyItemService = historyItemService;
        this.reportScheduleService = reportScheduleService;
        this.clusterConfigService = clusterConfigService;
        this.scheduler = scheduler;

        final AggregatesConfig config = clusterConfigService.getOrDefault(AggregatesConfig.class,
                AggregatesConfig.defaultConfig());

        this.config = new AtomicReference<>(config);

        eventBus.register(this);
    }

    @Override
    public void doRun() {
        LOG.debug("Config: [{}]", config.get());

        if (config.get().purgeHistory()){
            purgeHistory();
        }

        if (config.get().resolveOrphanedAlerts()) {
            resolveOrphanedAlerts();
        }

    }


    private void purgeHistory(){
        Calendar cal = Calendar.getInstance();

        List<ReportSchedule> reportSchedules = reportScheduleService.all();
        int retention = AggregatesUtil.timespanToSeconds(config.get().historyRetention(), cal);

        for (ReportSchedule reportSchedule : reportSchedules) {
            int timespan = AggregatesUtil.timespanToSeconds(reportSchedule.getTimespan(), cal);
            if (timespan > retention) {
                retention = timespan;
            }
        }

        LOG.debug("Retention is set to " + retention + " seconds (" + new Duration(retention) + ")");

        cal.add(Calendar.SECOND, -1 * retention);

        //remove all items before the current date - retention time
        long initialCount = historyItemService.count();
        historyItemService.removeBefore(cal.getTime());
        LOG.info("Removed " + (initialCount - historyItemService.count()) + " history items");

    }

    private void resolveOrphanedAlerts() {
        List<Rule> rules = ruleService.all();

        List<String> streamIds = new ArrayList<String>();
        for (Rule rule : rules) {
            if (rule.getAlertConditionId() != null && !streamIds.contains(rule.getStreamId())) {
                streamIds.add(rule.getStreamId());
            }

            if (rule.getAlertConditionId() == null){
                LOG.warn("Rule [{}] has no associated AlertCondition, perhaps it was created with an old version of the plugin. Please re-create it to be able to generate alerts.", rule.getName());
            }
        }

        LOG.info("Removing Aggregate Alert Conditions that don't have associated rule.");
        for (String streamId : streamIds) {
            LOG.debug("Checking stream [{}]", streamId);
            try {
                Stream triggeredStream = streamService.load(streamId);

                List<AlertCondition> alertConditions = streamService.getAlertConditions(triggeredStream);
                for (AlertCondition alertCondition : alertConditions) {
                    LOG.debug("Checking for alert like AlertScanner does");
                    LOG.debug("AlertCondition: [{}]", alertCondition.getTitle());
                    Optional<Alert> alert = alertService.getLastTriggeredAlert(triggeredStream.getId(), alertCondition.getId());
                    if (alert.isPresent()){
                        LOG.debug("Alert found: [{}].", alert);
                    } else {
                        LOG.debug("Alert not found.");
                    }

                    LOG.debug("Checking alert condition [{}] with type [{}]", alertCondition.getId(), alertCondition.getType());
                    if (alertCondition.getType().equals(AggregatesUtil.ALERT_CONDITION_TYPE)) {
                        LOG.debug("Type matches [{}], verifying if there's a rule with this alert condition", AggregatesUtil.ALERT_CONDITION_TYPE);
                        boolean found = false;
                        Rule foundRule = null;
                        for (Rule rule : rules) {
                            LOG.debug("Rule alertConditionId: [{}], alertConditionId: [{}]", rule.getAlertConditionId(), alertCondition.getId());
                            if (alertCondition.getId().equals(rule.getAlertConditionId())) {
                                foundRule = rule;
                                break;
                            }
                        }
                        if (foundRule == null) {
                            LOG.warn("Removing alertCondition [{}] of type [{}] because it's orphaned.", alertCondition.getTitle(), alertCondition.getType());
                            streamService.removeAlertCondition(triggeredStream, alertCondition.getId());
                        } else {
                            LOG.debug("AlertCondition [{}] is bound to rule [{}].", alertCondition.getTitle(), foundRule.getName());
                        }
                    }
                }


            } catch (NotFoundException e) {
                LOG.error("Stream with ID [{}] not found, skipping.", streamId);
                continue;
            }

        }

        //id, state, skip, limit
        LOG.info("Resolving unresolved Aggregate Alerts that don't have associated rule.");
        List<Alert> alerts = alertService.listForStreamIds(streamIds, Alert.AlertState.UNRESOLVED, 0, 100);
        for (Alert alert : alerts) {

            LOG.debug("Alert found [{}] with alertCondition [{}]", alert.getDescription(), alert.getConditionId());
            String alertConditionId = alert.getConditionId();

            try {
                Stream triggeredStream = streamService.load(alert.getStreamId());
                if (streamService.getAlertCondition(triggeredStream, alertConditionId).getType().equals(AggregatesUtil.ALERT_CONDITION_TYPE)) {
                    Rule foundRule = null;
                    for (Rule rule : rules) {
                        if (alertConditionId.equals(rule.getAlertConditionId())) {
                            foundRule = rule;
                            break;
                        }
                    }
                    if (foundRule == null) {
                        LOG.warn("Alert [{}] not bound to any rule, resolving", alert.getDescription());
                        alertService.resolveAlert(alert);
                    } else {
                        LOG.debug("Alert [{}] bound to rule [{}]", alert.getDescription(), foundRule.getName());
                    }
                } else {
                    LOG.debug("Alert [{}] does not belong to Aggregates AlertCondition", alert.getDescription());
                }
            } catch (NotFoundException e) {
                LOG.warn("Alert Condition not found for alert [{}], resolving", alert.getDescription());
                alertService.resolveAlert(alert);
            }
        }


    }




    @Override
    public int getInitialDelaySeconds() {
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

    @Subscribe
    @SuppressWarnings("unused")
    public void updateConfig(ClusterConfigChangedEvent event) {
        if (!AggregatesConfig.class.getCanonicalName().equals(event.type())) {
            return;
        }
        scheduler.schedule((Runnable) this::reload, 0, TimeUnit.SECONDS);
        LOG.info("AggregatesConfig updated");
    }

    private void reload() {
        final AggregatesConfig newConfig = clusterConfigService.getOrDefault(AggregatesConfig.class,
                AggregatesConfig.defaultConfig());

        LOG.info("Updating AggregatesConfig - {}", newConfig);
        config.set(newConfig);
        //filterEngine.set(new AggregatesConfig(newConfig, metricRegistry));
    }
}
