package esb.camel_docker_esb;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SPF_US extends RouteBuilder {
	
    @Override
    public void configure() {
    	from("cxf:bean:spfEndpoint")
    		.routeId("spf_us").startupOrder(2) // ensures spf_ds is started first
    		.to("jms:spf_ds");
    }

}
