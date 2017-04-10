import packageJson from '../../package.json'
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin'

import AggregatesPage from 'aggregates/AggregatesPage'
import SchedulesPage from 'aggregates/SchedulesPage'

PluginStore.register(new PluginManifest(packageJson, {

  routes: [
    { path: '/aggregates', component: AggregatesPage, permissions: 'AGGREGATES_READ' },
    { path: '/aggregates/schedules', component: SchedulesPage, permissions: 'AGGREGATES_READ' }
  ],

  navigation: [
    { path: '/aggregates', description: 'Aggregates' }
  ]

}))
