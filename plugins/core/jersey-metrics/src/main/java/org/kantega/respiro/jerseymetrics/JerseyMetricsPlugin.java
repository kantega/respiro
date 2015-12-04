package org.kantega.respiro.jerseymetrics;

import com.codahale.metrics.MetricRegistry;
import org.glassfish.jersey.server.ResourceConfig;
import org.kantega.respiro.jersey.ApplicationCustomizer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

/**
 *
 */
@Plugin
public class JerseyMetricsPlugin implements ApplicationCustomizer {


    public static MetricRegistry metricRegistry;

    @Export
    private final ApplicationCustomizer metricCustomizer = this;

    public JerseyMetricsPlugin(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public static MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    @Override
    public void customize(ResourceConfig resourceConfig) {
        resourceConfig.register(TimerFeature.class);
        resourceConfig.register(AroundWriteMeter.class);
    }
}
