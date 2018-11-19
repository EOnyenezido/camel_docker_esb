package esb.camel_docker_esb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.MessageHistory;
import org.apache.camel.TypeConverter;
import org.springframework.stereotype.Component;

import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceResultMsg;

/**
 * A bean that returns a custom error message when you call the {@link #generateTimeoutResponse()} method.
 * <p/>
 * Uses <tt>@Component("timeoutResponse")</tt> to register this bean with the name <tt>myBean</tt>
 * that we use in the Camel route to lookup this bean.
 */
@Component("buildLogIndex")
public class BuildLogIndex {
    
    public Map<String, String> generateLogMessage(QueryBalanceResultMsg body, @Headers Map<String, String> headers, Exchange exchange, TypeConverter converter)	{
    	
    	// Log to be sent to Elastic Search
    	Map<String, String> map = new HashMap<String, String>();
    	
    	// Save all the headers as an entry
    	for (Map.Entry<String, String> entry : headers.entrySet())
    	{
    	    map.put("header_" + entry.getKey(), entry.getValue());
    	}
    	
    	// Get important parts from the body
    	map.put("TransactionID", body.getResultHeader().getTransactionId());
    	map.put("ResultCode", body.getResultHeader().getResultCode());
    	map.put("ResultDescription", body.getResultHeader().getResultDesc());
    	
    	// Get the time the request was received and the number of retries
    	map.put("Time", exchange.getProperty(Exchange.CREATED_TIMESTAMP, String.class));
    	map.put("NoOfRetries", exchange.getProperty(Exchange.REDELIVERY_COUNTER, String.class));
    	
    	// Save the full response message
		map.put("ResponseMessage", converter.convertTo(String.class, body));
		
		// Add duration
		Long duration = 0L;
		Long durationOnDownstream = 0L;
		@SuppressWarnings("unchecked")
		List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
		for (MessageHistory item : list)	{
			duration = duration + item.getElapsed();
			if (item.getNode().getLabel().equals("cxf:bean:downstreamCBS"))	{ // log duration for calling downstream system
				durationOnDownstream = item.getElapsed();
			}
			map.put(item.getNode().getLabel(), Long.toString(item.getElapsed())); // log duration on each route
		}
		map.put("TotalDuration(MS)", duration.toString());
		map.put("DurationOnDownstream(MS)", durationOnDownstream.toString());
		
		// Return the final log message
		return map;
    }

}
