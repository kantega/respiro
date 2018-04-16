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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.kantega.respiro.dummy.DummyContentFilter.getFilteredContent;

public class DummiesServlet extends HttpServlet {

    private final List<Rule> invocations = new ArrayList<>();

    private final List<Rule> rules = new ArrayList<>();
    
    private final Map<String, String> routingTable = new HashMap<>();

    public List<String> getPaths() {

        return rules.stream().map(Rule::getPath).collect(Collectors.toList());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getRequestURI().equals("/dummies/invocations")) {
            if(req.getMethod().equals("GET"))
                createInvocationsResponse(req, resp);
            else if(req.getMethod().equals("DELETE"))
                invocations.clear();
            else
                resp.sendError(405, "Method " + req.getMethod() + " not supported on " + req.getRequestURI());
            return;
        }
        
        if(req.getRequestURI().equals("/dummies/router")){
            
            final String soapAction = req.getHeader("SOAPAction");
            final Optional<String> redirect = getRouteRedirect(soapAction);
            if( redirect.isPresent())
                
                resp.sendRedirect(format("http://localhost:%s%s",System.getProperty("reststopPort"),redirect.get()));
            else
                resp.sendError(400, "Found no matching route for SOAPAction header "+ soapAction);
            
            return;
        }

        for (Rule rule : rules) {
            if (rule.matches(req)) {
                invocations.add(rule);
                resp.setStatus(rule.getResponseCode());
                resp.setContentType(rule.getContentType());
                for (String header : rule.getResponseHeaders().keySet())
                    resp.setHeader(header, getFilteredContent(rule.getResponseHeaders().get(header)));

                resp.getOutputStream().write(Files.readAllBytes(rule.getResponseFile().toPath()));
                return;
            }
        }
        resp.sendError(400, "Found no matching rule for request.");
    }

    private void createInvocationsResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Stream<Rule> invStream = invocations.stream();
        if (req.getParameter("method") != null)
            invStream = invStream.filter(r -> r.getMethod().equals(req.getParameter("method")));
        if (req.getParameter("status") != null)
            invStream = invStream.filter(r -> r.getResponseCode() == Integer.parseInt(req.getParameter("status")));

        String response = "["+invStream.map(this::toJson).collect(Collectors.joining(","))+"]";
        resp.setContentType("application/json");
        resp.getOutputStream().write(response.getBytes());
    }


    public void addRESTEndpoints(File dir, Properties props) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        File[] files = dir.listFiles(f -> f.getName().endsWith("-rule.xml"));
        for (File file : files) {
            rules.add(new Rule(file));
        }

    }
    
    public void addSOAPHeaderRoutingTable(File routingTable) throws IOException {
        final Properties p = new Properties();
        p.load(new FileInputStream(routingTable));
        for (final String name: p.stringPropertyNames())
            this.routingTable.put(name, p.getProperty(name));
    }

    private String toJson(Rule r) {

        return format("{\"method\":\"%s\",\"uri\":\"%s\",\"contentType\":\"%s\",\"status\":%d}",
            r.getMethod(), r.getPath(), r.getContentType(), r.getResponseCode());
        
    }

    class Rule {

        private final String path;
        private final String method;
        private final String contentType;
        private final int responseCode;
        private final File responseFile;
        private final Map<String, String> responseHeaders = new HashMap<>();

        public Rule(File ruleFile) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
            final Document doc = newInstance().newDocumentBuilder().parse(ruleFile);
            path = doc.getDocumentElement().getElementsByTagName("path").item(0).getTextContent();
            method = doc.getDocumentElement().getElementsByTagName("method").item(0).getTextContent();
            contentType = doc.getDocumentElement().getElementsByTagName("content-type").item(0).getTextContent();
            responseCode = parseInt(doc.getDocumentElement().getElementsByTagName("response-code").item(0).getTextContent());

            final String responseFileName = ruleFile.getName().substring(0, ruleFile.getName().indexOf("-rule.xml")) + "-response." + mapSuffix(contentType);
            responseFile = new File(ruleFile.getParentFile(), responseFileName);

            final NodeList headers = doc.getDocumentElement().getElementsByTagName("response-headers");
            for (int i = 0; i < headers.getLength(); i++)
                responseHeaders.put(headers.item(i).getFirstChild().getNextSibling().getNodeName(), headers.item(i).getTextContent());

        }

        public Map<String, String> getResponseHeaders() {
            return responseHeaders;
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }

        public String getContentType() {
            return contentType;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public File getResponseFile() {
            return responseFile;
        }

        private String mapSuffix(String contentType) {
            String[] ct = contentType.split(";");
            for (String content : ct) {
                if ("application/json".equals(content))
                    return "json";
                else if ("application/xml".equals(content))
                    return "xml";
                else if ("text/plain".equals(content))
                    return "txt";
                else if ("text/html".equals(content))
                    return "html";
            }
            throw new IllegalArgumentException("Unsupported content type: " + contentType);

        }

        public boolean matches(HttpServletRequest req) {

            StringBuilder requestURI = new StringBuilder(req.getRequestURI());
            if (req.getQueryString() != null && req.getQueryString().trim().length() > 0)
                requestURI.append("?").append(req.getQueryString());

            return this.method.equals(req.getMethod()) && this.path.equals(requestURI.toString());
        }
    }

    public Optional<String> getRouteRedirect(String soapAction) {

        
        if( soapAction == null) return Optional.empty();

        return routingTable.keySet().stream()
            .filter(s -> soapAction.matches(s))
            .map(routingTable::get).findFirst();
    }
}
