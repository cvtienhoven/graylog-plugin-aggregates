package org.graylog.plugins.aggregates;


/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class Aggregates{//} extends Periodical {
    /*
    private int sequence = 0;
    private int maxInterval = 1; // max interval detected in rules

    private final ClusterConfigService clusterConfigService;
    private final Searches searches;
    private final Cluster cluster;
    private final RuleService ruleService;
    private final HistoryItemService historyItemService;
    private final AlertConditionFactory alertConditionFactory;
    private final StreamService streamService;
    private final AlertService alertService;

    private static final Logger LOG = LoggerFactory.getLogger(Aggregates.class);
    private List<Rule> list;

    @Inject
    public Aggregates(Searches searches, ClusterConfigService clusterConfigService,
                      Cluster cluster, RuleService ruleService, HistoryItemService historyItemService, AlertConditionFactory alertConditionFactory,
                      StreamService streamService, AlertService alertService) {
        this.searches = searches;
        this.clusterConfigService = clusterConfigService;
        this.cluster = cluster;
        this.ruleService = ruleService;
        this.historyItemService = historyItemService;
        this.alertConditionFactory = alertConditionFactory;
        this.streamService = streamService;
        this.alertService = alertService;
    }

    @VisibleForTesting
    boolean shouldRun() {
        return cluster.isHealthy();
    }


    @Override
    public void doRun() {

        if (!shouldRun()) {
            LOG.warn("Indexer is not running, not checking any rules this run.");
        } else {
            list = ruleService.all();

            if (sequence == maxInterval) {
                sequence = 0;
            }

            sequence++;

        }

    }

    @VisibleForTesting
    TimeRange buildRelativeTimeRange(int range) {
        try {
            return restrictTimeRange(RelativeRange.create(range));
        } catch (InvalidRangeParametersException e) {
            LOG.warn("Invalid timerange parameters provided, not executing rule");
            return null;
        }
    }

    protected org.graylog2.plugin.indexer.searches.timeranges.TimeRange restrictTimeRange(
            final org.graylog2.plugin.indexer.searches.timeranges.TimeRange timeRange) {
        final DateTime originalFrom = timeRange.getFrom();
        final DateTime to = timeRange.getTo();
        final DateTime from;

        final SearchesClusterConfig config = clusterConfigService.get(SearchesClusterConfig.class);

        if (config == null || Period.ZERO.equals(config.queryTimeRangeLimit())) {
            from = originalFrom;
        } else {
            final DateTime limitedFrom = to.minus(config.queryTimeRangeLimit());
            from = limitedFrom.isAfter(originalFrom) ? limitedFrom : originalFrom;
        }

        return AbsoluteRange.create(from, to);
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
    */
}
