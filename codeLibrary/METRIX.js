/**
 * METRIX_V1.1 - for metered and influxdb-reporter channels
 *
 * @argument {object} [options] - optional, if NOT defined then load METRIX from globalChannelMap
 * @returns METRIX closure - automatically putting it in the globalChannelMap
 */

function METRIX(options) {

    var GCM_VARIABLE = "METRIX_REPORT_V1.1", // keep in line with CodeTemplate VERSION
        REPORTER_OPTIONS = "REPORTER_OPTIONS",
        REPORTER_LASTTICK = "REPORTER_LASTTICK",
        METRICS_REGISTRY = "METRICS_REGISTRY",
        metrix;

    if (options === undefined) {
        metrix = globalChannelMap.get(GCM_VARIABLE);
        if (! metrix) {
            throw("METRIX not defined.");
        }
    } else {
        metrix = {
            ///////////////////////////////////////// reporting to influxdb

            //////// helpers
            reporterName: function() {
                return "REPORTER::" + channelId;
            },

            //////// instrumented channel methods

            getMetricsRegistry: function(recreate) {
                // the mr is in the globalChannelMap - independent of the closure !
                var mr = globalChannelMap.get(METRICS_REGISTRY);
                if (!mr || recreate) {
                    mr = new Packages.com.codahale.metrics.MetricRegistry();
                    globalChannelMap.put(METRICS_REGISTRY, mr);
                }
                return mr;
            },

            getInfluxdbReporter: function(mr, influxdbName, categoriesArray) {
                // builds a InfluxdbReporter
                var configName = "INFLUXDB::" + influxdbName,
                    // server::port::user::password
                    pattern = /^([0-9a-z\-\.]+)::([0-9]+)::(.+)::(.+)$/i,
                    config = configurationMap.get(configName),
                    cp = pattern.exec(config),
                    protocol, args, categories, reporterBuilder, reporter,
                    serverId = Packages.com.mirth.connect.server.controllers.ConfigurationController.getInstance().getServerId();
                if (config && cp !== null) {

                    protocol = Packages.metrics_influxdb.api.protocols.InfluxdbProtocols
                        .http(cp[1], cp[2], cp[3], cp[4], influxdbName);

                    reporterBuilder = Packages.metrics_influxdb.InfluxdbReporter.forRegistry(mr)
                        .protocol(protocol)
                        .tag("server", serverId)
                        .tag("channel", channelId);

                    if (Array.isArray(categoriesArray) && categoriesArray.length > 0) {
                        args = categoriesArray.map(function(cat) {
                            return '"' + cat + '"';
                        }).join(", ");
                        categories = eval("new Packages.metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer(" + args + ")");
                        if (categories instanceof Packages.metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer) {
                            reporterBuilder.transformer(categories);
                        }
                    }

                    reporter = reporterBuilder.build();
                } else {
                    throw ("Bad or missing server::port::user::password value in configurationMap[" + configName + "]");
                }
                return reporter;
            },

            setReporter: function(reporter) {
                // reporter shall implement report() and close() methods
                globalMap.put(this.reporterName(), reporter)
            },

            removeReporter: function() {
                // cleanup globalMap - used in channel undeploy
                var reporterName = this.reporterName(),
                    reporter = globalMap.get(reporterName);
                if (reporter && reporter.close) {
                    reporter.close();;
                } else {
                    logger.error("no close method found for " + reporterName + ". skipping.")
                }
                globalMap.remove(reporterName);
            },


            //////// reporter channel methods
            reporterDeploy: function(tick, debug) {
                //// prepare - used in reporter channel deploy
                // tick in seconds - will generate a JSON info message every tick
                tick = tick || 15 * 60;
                // debug boolean - some logger.debug
                debug = debug ? true : false;

                var now = new Date().getTime();
                globalChannelMap.put(REPORTER_OPTIONS, {
                    start: now,
                    "debug": debug,
                    "tick": tick * 1000
                });
                globalChannelMap.put(REPORTER_LASTTICK, 0);
            },

            reporterReport: function() {
                var
                // REPORTER::channelId
                regexp = /^REPORTER::[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/,
                    options = globalChannelMap.get(REPORTER_OPTIONS) || {},
                    lastTick = globalChannelMap.get(REPORTER_LASTTICK) || 0,
                    now = new Date().getTime(),
                    reports = [],
                    returnValue = java.util.ArrayList();

                globalMap.globalVariableMap.keySet().toArray().forEach(function(key) {
                    var reporter;
                    if (regexp.test(key)) {
                        reporter = globalMap.get(key);
                        if (reporter && reporter.report) {
                            reporter.report();
                        } else {
                            logger.error("no report method found for " + key + ". skipping.")
                        }
                        reports.push(key);
                        if (options.debug) logger.debug("reporting " + key);
                    }
                });

                if (options.tick) {
                    if (now - lastTick > options.tick) {
                        globalChannelMap.put(REPORTER_LASTTICK, now);
                        returnValue.add(JSON.stringify({
                            "uptimeHrs": (now - options.start) / 1000 / 3600,
                            "tick": options.tick / 1000,
                            "reports": reports
                        }));
                    } else {
                        if (options.debug) logger.debug("just reporting. no tick.")
                    }
                }

                return returnValue;
            }
        };
        globalChannelMap.put(GCM_VARIABLE, metrix);
    };
    return metrix;
}