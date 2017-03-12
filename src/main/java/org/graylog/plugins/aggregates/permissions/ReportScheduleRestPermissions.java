package org.graylog.plugins.aggregates.permissions;

import com.google.common.collect.ImmutableSet;
import org.graylog2.plugin.security.Permission;
import org.graylog2.plugin.security.PluginPermissions;

import java.util.Collections;
import java.util.Set;

import static org.graylog2.plugin.security.Permission.create;

public class ReportScheduleRestPermissions implements PluginPermissions {
    public static final String AGGREGATE_REPORT_SCHEDULES_READ = "aggregate_report_schedules:read";
    public static final String AGGREGATE_REPORT_SCHEDULES_CREATE = "aggregate_report_schedules:create";
    public static final String AGGREGATE_REPORT_SCHEDULES_UPDATE = "aggregate_report_schedules:update";
    public static final String AGGREGATE_REPORT_SCHEDULES_DELETE = "aggregate_report_schedules:delete";

    private final ImmutableSet<Permission> permissions = ImmutableSet.of(
            create(AGGREGATE_REPORT_SCHEDULES_READ, "Read aggregate report schedules"),
            create(AGGREGATE_REPORT_SCHEDULES_CREATE, "Create aggregate report schedules"),
            create(AGGREGATE_REPORT_SCHEDULES_UPDATE, "Update aggregate report schedules"),
            create(AGGREGATE_REPORT_SCHEDULES_DELETE, "Delete aggregate report schedules")
    );

    @Override
    public Set<Permission> permissions() {
        return permissions;
    }

    @Override
    public Set<Permission> readerBasePermissions() {
        return Collections.emptySet();
    }
}