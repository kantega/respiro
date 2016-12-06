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

package org.kantega.respiro.ui.metrics;

import org.kantega.respiro.ui.UiModule;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;

/**
 *
 */
@Plugin
public class MetricsUIPlugin {

    @Export final UiModule uiModule = () -> "metrics.js";
    @Export final Filter js;
    @Export final Filter html;


    public MetricsUIPlugin(@Config(defaultValue = "/respiro") String respiroPath,
                           ServletBuilder servletBuilder) {

        String respiroDir = respiroPath + "/";

        js = servletBuilder.resourceServlet( getClass().getResource("/metrics/metrics.js"), respiroDir +"metrics.js");

        html = servletBuilder.resourceServlet(getClass().getResource("/metrics/metrics.html"), respiroDir +"partials/metrics.html");
    }
}
