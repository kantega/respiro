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

package org.kantega.respiro.logging;

import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.apache.cxf.jaxws.EndpointImpl;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.xml.ws.Endpoint;

@Plugin
public class LoggingPlugin implements EndpointCustomizer {

    @Export
    final EndpointCustomizer endpointCustomizer = this;


    public LoggingPlugin() {

    }

    @Override
    public void customizeEndpoint(Endpoint endpoint) {
        EndpointImpl impl = (EndpointImpl) endpoint;
        impl.getOutFaultInterceptors().add(new LogCorrelationInterceptor());
        impl.getOutFaultInterceptors().add(new FaultLoggingInterceptor());
    }
}
