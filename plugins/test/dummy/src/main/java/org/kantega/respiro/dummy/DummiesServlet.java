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
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.copy;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

public class DummiesServlet extends HttpServlet {

    private List<Rule> rules = new ArrayList<>();

    public List<String> getPaths() {

        return rules.stream().map(Rule::getPath).collect(Collectors.toList());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        for (Rule rule : rules) {
            if (rule.matches(req)) {
                resp.setStatus(rule.getResponseCode());
                resp.setContentType(rule.getContentType());
                copy(rule.getResponseFile().toPath(), resp.getOutputStream());
                return;
            }
        }
        resp.sendError(400, "Found no matching rule for request.");
    }

    public void addRESTEndpoints(File dir, Properties props) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        File[] files = dir.listFiles(f -> f.getName().endsWith("-rule.xml"));
        for (File file : files) {
            rules.add(new Rule(file));
        }

    }

    class Rule {

        private final String path;
        private final String method;
        private final String contentType;
        private final int responseCode;
        private final File responseFile;

        public Rule(File ruleFile) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
            Document doc = newInstance().newDocumentBuilder().parse(ruleFile);
            path = doc.getDocumentElement().getElementsByTagName("path").item(0).getTextContent();
            method = doc.getDocumentElement().getElementsByTagName("method").item(0).getTextContent();
            contentType = doc.getDocumentElement().getElementsByTagName("content-type").item(0).getTextContent();
            responseCode = parseInt(doc.getDocumentElement().getElementsByTagName("response-code").item(0).getTextContent());

            String responseFileName = ruleFile.getName().substring(0, ruleFile.getName().indexOf("-rule.xml")) + "-response." + mapSuffix(contentType);
            responseFile = new File(ruleFile.getParentFile(), responseFileName);


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
            }
            throw new IllegalArgumentException("Unsupported content type: " + contentType);

        }

        public boolean matches(HttpServletRequest req) {

            return this.method.equals(req.getMethod()) && this.path.equals(req.getRequestURI());
        }
    }
}
