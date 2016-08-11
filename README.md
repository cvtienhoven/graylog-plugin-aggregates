# Aggregates Plugin for Graylog

[![Build Status](https://travis-ci.org/cvtienhoven/graylog-plugin-aggregates.svg?branch=master)](https://travis-ci.org/cvtienhoven/graylog-plugin-aggregates)

**Required Graylog version:** 2.0 and later

The Aggregates Plugin for Graylog enables users to execute term searches and get notified when the given criteria are met. Currently, there are the following alert conditions in Graylog:

* Message count
* Field value
* Field content

However, these conditions will not be sufficient to match the following scenario:

**_Send an alert when someone fails to login from the same source ip 3 or more times in the past minute._**

This scenario is actually very useful in a security context, but with the built-in alert conditions, it's not possible to match exactly this condition. The part "**_from the same source ip_**" is difficult to match. It would take an aggregate search that groups by value and returns the count per value. That's what the plugin aims to do. You can configure rules as shown in the screenshots below.

**Create / edit a rule**

![](https://github.com/cvtienhoven/graylog-plugin-aggregates/blob/master/images/edit_rule.png)


**Rule overview**

![](https://github.com/cvtienhoven/graylog-plugin-aggregates/blob/master/images/list.png)


**Alert example**

![](https://github.com/cvtienhoven/graylog-plugin-aggregates/blob/master/images/aggregates_alert.png)



Installation
------------

[Download the plugin](https://github.com/cvtienhoven/graylog-plugin-aggregates/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Development
-----------

You can improve your development experience for the web interface part of your plugin
dramatically by making use of hot reloading. To do this, do the following:

* `git clone https://github.com/Graylog2/graylog2-server.git`
* `cd graylog2-server/graylog2-web-interface`
* `ln -s $YOURPLUGIN plugin/`
* `npm install && npm start`

Usage
-----

Use the Aggregates tab in the web interface of Graylog to define rules with alert criteria. For non-admin users, there are the following permissions that should be configured (via the REST API) to be able to fully (or partly) operate the plugin:

* aggregate_rules:read
* aggregate_rules:create
* aggregate_rules:update
* aggregate_rules:delete

Each rule can be configured to be executed on a particular stream, or on "No Stream", e.g. a global search. For the latter option to be present, the user needs to be able to have at least the following permissions:

* searches:absolute
* searches:relative
* searches:keyword

Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

Plugin Release
--------------

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
