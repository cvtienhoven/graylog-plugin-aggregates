package org.graylog.plugins.aggregates.permissions;

import com.google.common.collect.ImmutableSet;
import org.graylog2.plugin.security.Permission;
import org.graylog2.plugin.security.PluginPermissions;

import java.util.Collections;
import java.util.Set;

import static org.graylog2.plugin.security.Permission.create;

public class RuleRestPermissions implements PluginPermissions {
    public static final String AGGREGATE_RULES_READ = "aggregate_rules:read";
    public static final String AGGREGATE_RULES_CREATE = "aggregate_rules:create";
    public static final String AGGREGATE_RULES_UPDATE = "aggregate_rules:update";
    public static final String AGGREGATE_RULES_DELETE = "aggregate_rules:delete";

    private final ImmutableSet<Permission> permissions = ImmutableSet.of(
            create(AGGREGATE_RULES_READ, "Read aggregate rules"),
            create(AGGREGATE_RULES_CREATE, "Create aggregate rules"),
            create(AGGREGATE_RULES_UPDATE, "Update aggregate rules"),
            create(AGGREGATE_RULES_DELETE, "Delete aggregate rules")
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