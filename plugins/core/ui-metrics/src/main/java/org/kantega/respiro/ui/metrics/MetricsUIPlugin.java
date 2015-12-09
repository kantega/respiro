package org.kantega.respiro.ui.metrics;

import org.kantega.respiro.ui.UiModule;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;

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

        js = servletBuilder.resourceServlet(respiroDir +"metrics.js", getClass().getResource("/metrics/metrics.js"));

        html = servletBuilder.resourceServlet(respiroDir +"partials/metrics.html", getClass().getResource("/metrics/metrics.html"));
    }
}
