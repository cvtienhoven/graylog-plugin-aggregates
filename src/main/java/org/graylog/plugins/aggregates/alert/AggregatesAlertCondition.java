
package org.graylog.plugins.aggregates.alert;

import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import java.util.Map;

public class AggregatesAlertCondition extends AbstractAlertCondition {
    private String description = "Dummy alert to test notifications";


    public AggregatesAlertCondition(Rule rule, String description, Stream stream, String id, String type, DateTime createdAt, String creatorUserId, Map<String, Object> parameters, String title) {
        super(stream, id, AggregatesUtil.getAlertConditionType(rule), createdAt, creatorUserId, parameters, title);
        this.description = description;
    }

    protected AggregatesAlertCondition(Stream stream, String id, String type, DateTime createdAt, String creatorUserId, Map<String, Object> parameters, String title) {
        super(stream, id, type, createdAt, creatorUserId, parameters, title);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CheckResult runCheck() {
        return new CheckResult(true, this, this.description, Tools.nowUTC(), null);
    }
}
