package org.graylog.plugins.aggregates.alerts;

import org.graylog2.alerts.AlertSender;

public interface AggregatesAlertSender extends AlertSender {
    //only used for decoupling the AggregatesEmailAlarmCallback from the original EmailAlarmCallback and its binding to the AlertSender interface.
}
