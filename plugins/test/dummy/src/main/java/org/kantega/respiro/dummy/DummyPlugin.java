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

package org.kantega.respiro.dummy;

import org.kantega.respiro.api.EndpointBuilder;
import org.kantega.respiro.api.EndpointConfig;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;
import org.kantega.reststop.classloaderutils.Artifact;
import org.kantega.reststop.classloaderutils.PluginInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.kantega.reststop.api.FilterPhase.PRE_AUTHENTICATION;
import static org.kantega.reststop.classloaderutils.PluginInfo.parse;

@Plugin
public class DummyPlugin {

    private final String basedir = getProperty("reststopPluginDir");
    private final String DUMMY_PROPS = "dummy.properties";
    private final List<String> openServices = new ArrayList<>();

    @Export
    private final Collection<EndpointConfig> endpointConfigs;
    @Export
    private final Filter dummiesServlet;
    @Export
    private final Filter authFilter;


    public DummyPlugin(ServletContext servletContext, EndpointBuilder ecBuilder, ServletBuilder servletBuilder) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {


        DummiesServlet dummies = new DummiesServlet();
        this.dummiesServlet = servletBuilder.servlet(dummies, "/dummies/*");

        String moduleArtifactId = parseModuleArtifactId();
        try {
            File dummyBasedir = new File(basedir, "src/test/dummies");
            File[] dirs = dummyBasedir.listFiles(File::isDirectory);

            endpointConfigs = new ArrayList<>();
            if (dirs != null) {
                for (File dir : dirs) {
                    Properties props = new Properties();
                    props.load(new FileInputStream(new File(dir, DUMMY_PROPS)));
                    String style = props.getProperty("style", "SOAP").toUpperCase();
                    if ("SOAP".equals(style))
                        addSOAPEndpoint(servletContext, ecBuilder, dir, props, moduleArtifactId);
                    else if ("REST".equals(style)) {
                        dummies.addRESTEndpoints(dir, props);
                        if( "NONE".equals(props.getProperty("auth","BASIC")))
                            openServices.addAll(dummies.getPaths());
                    }else
                        throw new IllegalArgumentException(format("Unknown style %s. Should be one of REST, SOAP.", style));

                }
            }
        } catch (IOException | SAXException | XPathExpressionException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.authFilter = servletBuilder.filter(new DummyAuthFilter(openServices),"/*", PRE_AUTHENTICATION);
    }

    private String parseModuleArtifactId() {
        File pomXml = new File(getProperty("reststopPluginDir"), "pom.xml");

        try {
            Document doc = newInstance().newDocumentBuilder().parse(pomXml);

            return doc.getDocumentElement().getElementsByTagName("artifactId").item(0).getTextContent();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSOAPEndpoint(ServletContext servletContext, EndpointBuilder ecBuilder, File dir, Properties props, String moduleArtifactId) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        String servicePath = props.getProperty("path");
        String namespace = props.getProperty("namespace");
        String service = props.getProperty("service");
        String port = props.getProperty("port");
        String auth = props.getProperty("auth","BASIC");

        String wsdlLocation = props.getProperty("wsdl");
        URL wsdlURL;
        if (wsdlLocation.contains(":")) {
            String artifactId = wsdlLocation.substring(0, wsdlLocation.indexOf(":"));
            String path = wsdlLocation.substring(wsdlLocation.indexOf(":") + 1);
            wsdlURL = findWsdlInPlugin(servletContext, artifactId, path);
        } else {
            wsdlURL = findWsdlInPlugin(servletContext, moduleArtifactId, wsdlLocation);
        }


        File[] files = dir.listFiles(f -> f.getName().endsWith("-rule.xml"));

        DummyProvider provider = new DummyProvider(files);
        endpointConfigs.add(ecBuilder.endpoint(getClass(), provider)
                .wsdl(wsdlURL)
                .namespace(namespace)
                .wsdlService(service)
                .wsdlPort(port)
                .path(servicePath).build());

        if("NONE".equals(auth.toUpperCase()))
            openServices.add("/ws"+servicePath);
    }

    private URL findWsdlInPlugin(ServletContext servletContext, String artifactId, String path) {
        try {
            Document pluginsXml = (Document) servletContext.getAttribute("pluginsXml");

            List<PluginInfo> infos = parse(pluginsXml);

            for (PluginInfo info : infos) {
                if (artifactId.equals(info.getArtifactId())) {
                    List<URL> urls = new ArrayList<>();
                    for (Artifact runtime : info.getClassPath("test")) {
                        urls.add(runtime.getFile().toURI().toURL());
                    }
                    URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
                    URL resource = urlClassLoader.getResource(path);
                    urlClassLoader.close();
                    return resource;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        throw new RuntimeException("Could not find WSDL in plugin " + artifactId + " at path " + path);
    }
}
