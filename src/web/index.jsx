//eslint-disable-next-line no-unused-vars
import webpackEntry from 'webpack-entry';

import packageJson from '../../package.json'
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin'
import AggregatesConfig from 'aggregates/AggregatesConfig';
import AggregatesPage from 'aggregates/AggregatesPage'
import SchedulesPage from 'aggregates/SchedulesPage'

const manifest = new PluginManifest(packageJson, {

  routes: [
    { path: '/aggregates', component: AggregatesPage, permissions: 'AGGREGATE_RULES_READ' },
    { path: '/aggregates/schedules', component: SchedulesPage, permissions: 'AGGREGATE_REPORT_SCHEDULES_READ' }
  ],
  navigation: [
    { path: '/aggregates', description: 'Aggregates', permissions: 'aggregate_rules:read' }
  ],

  systemConfigurations: [
    {
      component: AggregatesConfig,
      configType: 'org.graylog.plugins.aggregates.config.AggregatesConfig',
    },
  ],
});

PluginStore.register(manifest);
