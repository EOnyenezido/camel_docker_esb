package esb.camel_docker_esb;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SPF_DS extends RouteBuilder {
	
	/**
     * This camel route exposes a JMS queue to receive requests from the upstream(US) route. It handles communication and transformation with
     * the downstream system.
     * The route is transacted to ensure reliable re-delivery
     */
	
    @Override
    public void configure() {
    	from("jms:spf_ds")
    		.routeId("spf_ds").startupOrder(1) // ensures spf_ds is started first
    		.to("cxf:bean:downstreamCBS");
    }

}
