package org.graylog.plugins.aggregates.util;

import com.google.common.collect.ImmutableSet;
import org.graylog2.audit.PluginAuditEventTypes;

import java.util.Set;

public class AuditEventTypes implements PluginAuditEventTypes {

    public static final String NAMESPACE = "aggregates:";

    public static final String AGGREGATES_RULE_CREATE = NAMESPACE + "aggregates_rule:create";
    public static final String AGGREGATES_RULE_UPDATE = NAMESPACE + "aggregates_rule:update";
    public static final String AGGREGATES_RULE_DELETE = NAMESPACE + "aggregates_rule:delete";
    public static final String AGGREGATES_REPORT_SCHEDULE_CREATE = NAMESPACE + "aggregates_report_schedule:create";
    public static final String AGGREGATES_REPORT_SCHEDULE_UPDATE = NAMESPACE + "aggregates_report_schedule:update";
    public static final String AGGREGATES_REPORT_SCHEDULE_DELETE = NAMESPACE + "aggregates_report_schedule:delete";

    private static final Set<String> EVENT_TYPES = ImmutableSet .<String>builder()
            .add(AGGREGATES_RULE_CREATE)
            .add(AGGREGATES_RULE_UPDATE)
            .add(AGGREGATES_RULE_DELETE)
            .add(AGGREGATES_REPORT_SCHEDULE_CREATE)
            .add(AGGREGATES_REPORT_SCHEDULE_UPDATE)
            .add(AGGREGATES_REPORT_SCHEDULE_DELETE)
            .build();

    @Override
    public Set<String> auditEventTypes() {
        return EVENT_TYPES;
    }
}
