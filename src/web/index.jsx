import packageJson from '../../package.json';
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import AggregatesPage from 'aggregates/AggregatesPage';


PluginStore.register(new PluginManifest(packageJson, {

  routes: [
    { path: '/aggregates', component: AggregatesPage, permissions: 'AGGREGATES_READ' },
    
  ],

  navigation: [
    { path: '/aggregates', description: 'Aggregates' },
  ]

}));
