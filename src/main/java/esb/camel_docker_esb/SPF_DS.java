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
    	from("seda:spf_ds")
    		.routeId("spf_ds").startupOrder(2) // ensures logger route is started first
    		.hystrix().id("downstreamCBS") // hystrix circuit breaker and bulkhead pattern to prevent downstream failures from cascading to the entire ESB
    			.hystrixConfiguration()
    				.circuitBreakerRequestVolumeThreshold(10) // number of requests failing that will trip the circuit
    				.metricsRollingPercentileWindowInMilliseconds(10000)
    				.executionTimeoutInMilliseconds(10000) // time before a single request times out
    				.circuitBreakerSleepWindowInMilliseconds(5000) // time before circuit breaker tries requests again
    			.end()
    			.to("cxf:bean:downstreamCBS") // call the downstream billing system server
    			.onFallback() // error response on timeout
    				.bean("timeoutResponse", "generateTimeoutResponse")
    		.end();
    }

}
