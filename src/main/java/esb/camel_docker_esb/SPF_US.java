package esb.camel_docker_esb;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceRequestMsg;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceResultMsg;

@Component
public class SPF_US extends RouteBuilder {
	
    @Override
    public void configure() {
    	// Log all transactions to our ELK logging queue
    	onCompletion()
    		.choice()  // Convert from JSON to POJO for the logger if request was JSON
				.when(simple("${header.requestDataType} == 'application/json'"))
					.unmarshal("json")
			.end()
			.to("seda:logger");
    	
    	// SOAP Endpoint exposed using CXF
    	from("cxf:bean:spfEndpoint")
    		.routeId("spf_us_soap").startupOrder(3) // ensures spf_ds is started first
    		.to("seda:spf_ds"); // to a JMS queue for processing
    	
    	// REST Endpoint exposed using Camel REST DSL
    	restConfiguration()
    		.component("spark-rest")
    		.contextPath("{{route.basePath}}")
    		.port("{{route.port}}")
    		.host("{{route.hostname}}")
    		.bindingMode(RestBindingMode.json_xml) // Accept both JSON and XML and convert to a POJO
    		.dataFormatProperty("prettyPrint", "true")
    		.enableCORS(true) // Allow other domains to call API
    		.apiContextPath("api-doc") // Swagger API documentation
    		.apiProperty("api.version", "{{api.version}}")
    		.apiProperty("api.title", "{{api.title}}")
    		.apiProperty("api.description", "{{api.description}}")
    		.apiProperty("api.contact.name", "{{api.contact.name}}");
    	
    	rest("{{route.url}}")
    		.post()
    			.type(QueryBalanceRequestMsg.class).outType(QueryBalanceResultMsg.class) // let jax-b and jackson know what to bind to
    			.route().routeId("spf_us_rest")
    			.setHeader("operationName", constant("QueryBalance")) // set operation, namespace and soapAction name because it is calling a soap webservice
    			.setHeader("operationNamespace", constant("http://www.huawei.com/bme/cbsinterface/cbs/accountmgr"))
    			.setHeader("soapAction", constant("QueryBalance"))
    			.setHeader("requestDataType", simple("${in.header.Content-Type}")) // save the type of request which was sent xml or json
    			.to("seda:spf_ds") // to same JMS queue as SOAP requests for processing
    			.choice()  // let camel know how to bind the response - jax-b or jackson
    				.when(simple("${header.requestDataType} == 'application/json'"))
    					.marshal("json")
    				.when(simple("${header.requestDataType} == 'application/xml'"))
    					.convertBodyTo(QueryBalanceResultMsg.class)
    			.end();
    }

}
