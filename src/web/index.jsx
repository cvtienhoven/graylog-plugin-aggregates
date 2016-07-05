import packageJson from '../../package.json';
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import AggregatesPage from 'aggregates/AggregatesPage';


PluginStore.register(new PluginManifest(packageJson, {
  /* This is the place where you define which entities you are providing to the web interface.
     Right now you can add routes and navigation elements to it.

     Examples: */

  // Adding a route to /sample, rendering YourReactComponent when called:

  routes: [
    { path: '/aggregates', component: AggregatesPage, permissions: 'AGGREGATES_READ' },
    
  ],

  // Adding an element to the top navigation pointing to /sample named "Sample":

  navigation: [
    { path: '/aggregates', description: 'Aggregates' },
  ]

}));
