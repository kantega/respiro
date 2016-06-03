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

package org.kantega.respiro.exchanges;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.collector.CollectionListener;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.exchanges.rest.ExchangesResource;
import org.kantega.respiro.ui.UiModule;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;

import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
@Plugin
public class ExchangesPlugin {


    @Export
    private final Collection<Filter> filters = new ArrayList<>();

    @Export
    final Application exhangesApp;

    @Export
    final UiModule uiModule;

    private final CollectionListener exchangesListener;

    public ExchangesPlugin(
      @Config(defaultValue = "/respiro") String respiroPath,
      ServletBuilder servletBuilder,
      ApplicationBuilder applicationBuilder) {

        String respiroDir = respiroPath + "/";

        Exchanges exchanges = new Exchanges();
        filters.add(servletBuilder.redirectServlet("/exchanges", "respiro/#/exchanges"));
        filters.add(servletBuilder.redirectServlet("/exchanges/", "../respiro/#/exchanges"));

        filters.add(servletBuilder.resourceServlet(respiroDir + "partials/exchanges.html", getClass().getResource("/exchanges/list.html")));
        filters.add(servletBuilder.resourceServlet(respiroDir + "partials/exchanges-details.html", getClass().getResource("/exchanges/details.html")));
        filters.add(servletBuilder.resourceServlet(respiroDir + "exchanges.js", getClass().getResource("/exchanges/exchanges.js")));

        exhangesApp = applicationBuilder.application()
          .singleton(new ExchangesResource(exchanges))
          .build();

        uiModule = () -> "exchanges.js";

        exchangesListener = exchanges::addExchange;
        Collector.addListener(exchangesListener);

    }

    @PreDestroy
    public void preDestroy() {
        Collector.removeListener(exchangesListener);
    }
}
