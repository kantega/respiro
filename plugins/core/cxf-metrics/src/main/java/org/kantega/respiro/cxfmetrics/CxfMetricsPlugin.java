package org.kantega.respiro.cxfmetrics;

import com.codahale.metrics.MetricRegistry;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 *
 */
@Plugin
public class CxfMetricsPlugin implements EndpointCustomizer {

    @Export final EndpointCustomizer endpointCustomizer = this;
    private final MetricRegistry metricRegistry;

    public CxfMetricsPlugin(MetricRegistry metricRegistry) {

        this.metricRegistry = metricRegistry;
    }



    @Override
    public void customizeEndpoint(Endpoint endpoint) {
        EndpointImpl e = (EndpointImpl) endpoint;

        e.getServer().getEndpoint().getInInterceptors().add(new TimingBeforeInterceptor(Phase.RECEIVE));
        e.getServer().getEndpoint().getOutInterceptors().add(new TimingAfterInterceptor(Phase.SEND, metricRegistry));
    }


    private class TimingBeforeInterceptor extends AbstractPhaseInterceptor<Message> {
        public TimingBeforeInterceptor(String phase) {
            super(phase);
        }

        @Override
        public void handleMessage(Message message) throws Fault {
            message.getExchange().put("time_before", System.nanoTime());
        }
    }

    private class TimingAfterInterceptor extends AbstractPhaseInterceptor<Message> {

        private final MetricRegistry registry;

        private TimingAfterInterceptor(String phase, MetricRegistry registry) {
            super(phase);
            this.registry = registry;
        }

        @Override
        public void handleMessage(Message message) throws Fault {
            Long time_before = (Long) message.getExchange().get("time_before");
            if(time_before != null) {
                QName operation = (QName) message.get(Message.WSDL_OPERATION);

                String requestUri = (String) message.getExchange().getInMessage().get(Message.REQUEST_URI);

                String name = name("SOAP", requestUri, operation.getLocalPart() );

                registry.timer(name).update(System.nanoTime() - time_before, TimeUnit.NANOSECONDS);

            }
        }
    }
}
