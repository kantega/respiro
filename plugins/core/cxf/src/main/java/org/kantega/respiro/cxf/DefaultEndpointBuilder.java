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

package org.kantega.respiro.cxf;

import org.kantega.respiro.api.EndpointBuilder;
import org.kantega.respiro.api.EndpointConfig;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Map;

import static javax.wsdl.factory.WSDLFactory.newInstance;

class DefaultEndpointBuilder implements EndpointBuilder {

    @Override
    public Build endpoint(Class clazz, Object impl) {
        return new DefaultBuild(clazz.getClassLoader(), impl);
    }

    private class DefaultBuild implements Build {
        private final Object service;
        private String path;
        private URL wsdl;
        private String wsdlService;
        private String wsdlPort;
        private ClassLoader classLoader;
        private String namespace;
        private Definition definition;


        public DefaultBuild(ClassLoader classLoader, Object service) {
            this.classLoader = classLoader;
            this.service = service;
        }

        @Override
        public Build path(String path) {
            this.path = path;
            return this;
        }

        @Override
        public Build wsdl(URL wsdl) {
            this.wsdl = wsdl;
            return this;
        }

        @Override
        public Build wsdlNamed(String name, String version) {
            this.wsdl = getResource("META-INF/wsdl/" + name + "-" + version + "/" + name + ".wsdl");
            return this;
        }

        @Override
        public Build wsdl(String path) {
            this.wsdl = getResource(path);
            return this;
        }

        private URL getResource(String path) {
            URL resource = classLoader.getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("Could not find WSDL at path: " + path);
            }
            return resource;
        }

        @Override
        public Build namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        @Override
        public Build wsdlService(String service) {
            this.wsdlService = service;
            return this;
        }

        @Override
        public Build wsdlPort(String port) {
            this.wsdlPort = port;
            return this;
        }

        @Override
        public EndpointConfig build() {

            if (wsdl == null) {
                throw new IllegalArgumentException("WSDL location is required for service");
            }

            if (namespace == null) {
                namespace = getDefinition().getTargetNamespace();
            }

            if (wsdlService == null) {
                Map<QName, Service> services = getDefinition().getServices();
                wsdlService = services.keySet().iterator().next().getLocalPart();
            }

            if (wsdlPort == null) {
                Map<QName, Service> services = getDefinition().getServices();
                Service object = services.values().iterator().next();

                wsdlPort = (String) object.getPorts().keySet().iterator().next();
            }

            return new EndpointConfig() {
                @Override
                public ClassLoader getClassLoader() {
                    return classLoader;
                }

                @Override
                public Object getImplementor() {
                    return service;
                }

                @Override
                public String getPath() {
                    return path;
                }

                @Override
                public URL getWsdl() {
                    return wsdl;
                }

                @Override
                public QName getWsdlService() {
                    if (wsdlService == null) {
                        new IllegalArgumentException("wsdlService must be specified on service");
                    }
                    return new QName(namespace, wsdlService);
                }

                @Override
                public QName getWsdlPort() {
                    if (wsdlPort == null) {
                        new IllegalArgumentException("wsdlPort must be specified on service");
                    }
                    return new QName(namespace, wsdlPort);
                }
            };
        }

        private Definition getDefinition() {
            try {
                return this.definition == null ? definition = newInstance().newWSDLReader().readWSDL(wsdl.toExternalForm()) : definition;
            } catch (WSDLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}