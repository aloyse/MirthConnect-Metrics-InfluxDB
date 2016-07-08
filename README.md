# MirthConnect-Metrics-InfluxDB
Mirth Connect with dropwizard's Metrics and InfluxDB reporter

[TOC]

## thanks to
* [dropwizard's metrics](http://metrics.dropwizard.io)
* [David Bernard](https://github.com/davidB/metrics-influxdb)

## Abstract
CodeTemplate library for easy instrumentation of channels and reporting to InfluxDB

The instrumented channel just configures the reporter an puts the reporter in the globalMap.
A dedicated reporting channel executes the report() on 

## CodeLibrary
The library handles all using a single closure.
### creation
	metrix=METRIX({});
returns a new closure and puts it automatically in globalChannleMap.
### reuse
	metrix=METRIX();
gets the closuser from the globalChannleMap

* * *

### Reporter Channel functions

#### reporterDeploy(tick, debug)
does ALL the deployment stuff for a Reporter Channel.
Use drag-and-drop code block "reporterDeploy"...

    // deploy 1 hour tick messages and debug on
    METRIX({}).reporterDeploy(3600, true);

    /* in in JSON source javascript writer  use this code:
    return METRIX().reporterReport();
    */

#### reporterReport()
use the code from previous comment in the source javascript writer...

    return METRIX().reporterReport();

This loops through all reporters and executes a report(). After tick seconds a JSON status message is generated.
```
{
  "uptimeHrs" : 0.6545530555555555,
  "tick" : 3600,
  "reports" : [
    "REPORTER::30aab292-0216-4071-b694-75f6c6bcaac3",
    "REPORTER::ff608456-3a58-43f0-ba95-176b117bd981"
  ]
}
```
et voilà


* * *

## CAVEATS

* logging is not configured with mirthconnect for metrics-influxdb: -> if nothing arrives it could be wrong credentials ...

## TODOs
* configure logging
* 
