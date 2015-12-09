package org.kantega.respiro.camel;

import org.apache.camel.CamelContext;

/**
 *
 */
public interface CamelContextCustomizer {
    void customize(CamelContext camelContext);
}
