package org.kantega.respiro.ui.resources;

import org.kantega.respiro.api.EndpointConfig;
import org.kantega.reststop.api.PluginExport;

/**
 *
 */
public class SoapEndpoint {
    private final String path;
    private final String serviceName;

    public SoapEndpoint(PluginExport<EndpointConfig> export) {
        this.path = export.getExport().getPath();
        this.serviceName= export.getExport().getWsdlService().getLocalPart();
    }

    public String getPath() {
        return path;
    }

    public String getServiceName() {
        return serviceName;
    }
}
