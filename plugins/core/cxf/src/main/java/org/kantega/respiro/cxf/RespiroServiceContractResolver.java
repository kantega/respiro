/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.cxf;

import org.apache.cxf.endpoint.ServiceContractResolver;
import org.kantega.respiro.api.EndpointConfig;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class RespiroServiceContractResolver implements ServiceContractResolver {

    @Override
    public URI getContractLocation(QName qname) {

        if (qname == null) {
            return null;
        }

        EndpointConfig configuration = CxfPlugin.currentConfig.get();

        if (qname.equals(configuration.getWsdlService())) {
            try {
                return configuration.getWsdl().toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return null;

    }
}
