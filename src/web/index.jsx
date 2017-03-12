import packageJson from '../../package.json'
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin'

import AggregatesPage from 'aggregates/AggregatesPage'
import SchedulesPage from 'aggregates/SchedulesPage'

PluginStore.register(new PluginManifest(packageJson, {

  routes: [
    { path: '/aggregates', component: AggregatesPage, permissions: 'AGGREGATE_RULES_READ,AGGREGATE_REPORT_SCHEDULES_READ' },
    { path: '/aggregates/schedules', component: SchedulesPage, permissions: 'AGGREGATE_REPORT_SCHEDULES_READ' }
  ],

  navigation: [
    { path: '/aggregates', description: 'Aggregates' }
  ]

}))
