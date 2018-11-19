package esb.camel_docker_esb;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class Logger extends RouteBuilder {
	
	/**
     * This camel route exposes a JMS queue to receive logs from any of the routes. These logs are then sent via a REST request to
     * be stored on Elasticsearch
     */
	
    @Override
    public void configure() {
    	
    	from("seda:logger")
    		.routeId("logger").startupOrder(1) // ensures this starts first before requests are processed
    		.hystrix().id("elasticSearchLogging") // hystrix circuit breaker and bulkhead pattern to prevent downstream failures from cascading to the entire ESB
    			.hystrixConfiguration()
    				.circuitBreakerRequestVolumeThreshold(10) // number of requests failing that will trip the circuit
    				.metricsRollingPercentileWindowInMilliseconds(10000)
    				.executionTimeoutInMilliseconds(10000) // time before a single request times out
    				.circuitBreakerSleepWindowInMilliseconds(5000) // time before circuit breaker tries requests again
    			.end()
    			//.marshal("json")
    			.bean("buildLogIndex", "generateLogMessage")
    			.to("elasticsearch-rest:custom_log?operation=Index&indexName=esb_logs&indexType=_doc") // send the log to the elk server
    			.log("This is the response from  ELK: ${body}")
    			.onFallback() // error response on timeout
    				.log("Hystrix ELK Fallback Triggered. This is the ERROR reason: ${exception.message}")
    		.end();
    }

}
