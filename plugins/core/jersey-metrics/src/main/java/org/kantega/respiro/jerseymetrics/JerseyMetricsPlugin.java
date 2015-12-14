/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
