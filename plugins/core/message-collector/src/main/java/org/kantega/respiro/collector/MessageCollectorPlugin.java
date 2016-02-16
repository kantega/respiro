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

package org.kantega.respiro.collector;

import org.kantega.respiro.collector.cxf.MessageCollectorCustomizer;
import org.kantega.respiro.collector.jaxrs.CollectingFeature;
import org.kantega.respiro.collector.jaxrs.ClientCollectingFilter;
import org.kantega.respiro.collector.jdbc.JdbcCollector;
import org.kantega.respiro.collector.mail.CollectingMailSenderCustomizer;
import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.kantega.respiro.cxf.api.ServiceCustomizer;
import org.kantega.respiro.jdbc.DataSourceCustomizer;
import org.kantega.respiro.jersey.ApplicationCustomizer;
import org.kantega.respiro.jersey.ClientCustomizer;
import org.kantega.respiro.mail.MailSenderCustomizer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;

import javax.servlet.Filter;

import java.util.Collection;

import static org.kantega.reststop.api.FilterPhase.PRE_UNMARSHAL;

@Plugin
public class MessageCollectorPlugin {

    @Export
    final EndpointCustomizer endpointMessageCollector;

    @Export
    final ServiceCustomizer serviceMessageCollector;

    @Export
    private final ClientCustomizer clientCustomizer;

    @Export
    private final ApplicationCustomizer applicationCustomizer;

    @Export
    private final DataSourceCustomizer dataSourceCustomizer;

    @Export
    private final Filter clearingFilter;

    @Export
    private final MailSenderCustomizer mailSenderCustomizer;

    public MessageCollectorPlugin(ServletBuilder servletBuilder) {

        MessageCollectorCustomizer customizer = new MessageCollectorCustomizer();
        serviceMessageCollector = customizer;
        endpointMessageCollector = customizer;

        clientCustomizer = cc -> cc.register(new ClientCollectingFilter());

        applicationCustomizer = (r) -> r.register(CollectingFeature.class);

        dataSourceCustomizer = new JdbcCollector();

        clearingFilter = servletBuilder.filter(new ClearingFilter(), "/*", PRE_UNMARSHAL);

        mailSenderCustomizer = new CollectingMailSenderCustomizer();
    }
}
