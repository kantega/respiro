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

import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.apache.cxf.jaxws.EndpointImpl;

import javax.xml.ws.Endpoint;

/**
 *
 */
public class ExhangesCustomizer implements EndpointCustomizer {
    private final Exchanges exchanges;

    public ExhangesCustomizer(Exchanges exchanges) {

        this.exchanges = exchanges;
    }

    @Override
    public void customizeEndpoint(Endpoint endpoint) {

        EndpointImpl e = (EndpointImpl) endpoint;
        ExchangesInterceptor inter = new ExchangesInterceptor(exchanges);
        e.getOutInterceptors().add(inter);
        e.getOutFaultInterceptors().add(inter);



    }

}
