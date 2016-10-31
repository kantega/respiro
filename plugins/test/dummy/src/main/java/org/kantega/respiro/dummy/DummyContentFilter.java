package org.kantega.respiro.dummy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 */
class DummyContentFilter {
    static String getFilteredContent(Path responseFile) throws IOException {

        String responseContent = new String(Files.readAllBytes(responseFile));


        for(String name : System.getProperties().stringPropertyNames()) {
            String replace=System.getProperty(name);
            responseContent = responseContent.replaceAll("\\$\\{"+name+"\\}", replace);
        }

        return responseContent;
    }
}
