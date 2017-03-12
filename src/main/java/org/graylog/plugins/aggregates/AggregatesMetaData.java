package org.graylog.plugins.aggregates;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class AggregatesMetaData implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return "org.graylog.plugins.aggregates.AggregatesPlugin";
    }

    @Override
    public String getName() {
        return "Aggregates";
    }

    @Override
    public String getAuthor() {
        return "Christiaan van Tienhoven";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/cvtienhoven/graylog-plugin-aggregates");
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Graylog Aggregates plugin";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(2, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
