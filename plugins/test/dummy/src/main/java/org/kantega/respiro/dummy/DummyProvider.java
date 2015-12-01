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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by helaar on 14.10.2015.
 */
@WebServiceProvider
@ServiceMode(Service.Mode.MESSAGE)
@BindingType("http://schemas.xmlsoap.org/wsdl/soap/http")
public class DummyProvider implements Provider<Source> {

    private List<Rule> dispatchRules = new ArrayList<>();

    @Resource
    private WebServiceContext context;

    public DummyProvider(File files[]) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        for (File file : files) {
            dispatchRules.add(new Rule(file));
        }
    }

    @Override
    public Source invoke(Source request) {
        QName operation = (QName) context.getMessageContext().get(MessageContext.WSDL_OPERATION);
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DOMResult domResult = new DOMResult();
        try {
            TransformerFactory.newInstance().newTransformer().transform(request, domResult);
            for (Rule dispatchRule : dispatchRules) {
                Element documentElement = ((Document) domResult.getNode()).getDocumentElement();
                if (dispatchRule.matches(operation, documentElement))
                    return dispatchRule.getResult();
            }
            throw new WebServiceException("No rules match request");
        } catch (TransformerException | XPathExpressionException e) {
            throw new RuntimeException("Failed to transform request", e);
        }
    }

    private static class Rule {
        XPathExpression xPathExpression;
        String operation;
        File responseFile;
        Map<String, String> namespaces = new HashMap<>();

        public Rule(File ruleFile) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ruleFile);
            String xpath = doc.getDocumentElement().getElementsByTagName("xpath").item(0).getTextContent();
            operation = doc.getDocumentElement().getElementsByTagName("operation").item(0).getTextContent();

            String responseFileName = ruleFile.getName().substring(0,ruleFile.getName().indexOf("-rule.xml"))+"-response.xml";
            responseFile = new File(ruleFile.getParentFile(), responseFileName);

            NodeList ns = doc.getDocumentElement().getElementsByTagName("namespace");

            for (int i = 0; i < ns.getLength(); i++)
                namespaces.put(ns.item(i).getAttributes().getNamedItem("prefix").getTextContent(), ns.item(i).getTextContent());
            XPath xPath = XPathFactory.newInstance().newXPath();

            xPath.setNamespaceContext(new DefaultNamespaceContext(namespaces));
            xPathExpression = xPath.compile(xpath);

        }

        public Source getResult() {
            return new StreamSource(responseFile);
        }

        public boolean matches(QName operation, Element documentElement) throws XPathExpressionException {

            if (!this.operation.equals(operation.getLocalPart())) return false;


            return Boolean.TRUE.equals(xPathExpression.evaluate(documentElement, XPathConstants.BOOLEAN));
        }

        private class DefaultNamespaceContext implements NamespaceContext {
            private Map<String, String> namespaces;

            public DefaultNamespaceContext(Map<String, String> namespaces) {

                this.namespaces = namespaces;
            }

            @Override
            public String getNamespaceURI(String prefix) {
                return namespaces.get(prefix);
            }

            @Override
            public String getPrefix(String namespaceURI) {
                for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                    if (entry.getValue().equals(namespaceURI))
                        return entry.getKey();
                }
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return namespaces.keySet().iterator();
            }
        }
    }
}
