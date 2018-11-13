package esb.camel_docker_esb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceRequestMsg;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceResultMsg;
import com.huawei.bme.cbsinterface.common.ResultHeader;

/**
 * A bean that returns a custom error message when you call the {@link #generateTimeoutResponse()} method.
 * <p/>
 * Uses <tt>@Component("timeoutResponse")</tt> to register this bean with the name <tt>myBean</tt>
 * that we use in the Camel route to lookup this bean.
 */
@Component("timeoutResponse")
public class CustomTimeoutResponse {

    @Value("${timeout.message}")
    private String message;
    
    @Value("${timeout.code}")
    private String code;
    
    public QueryBalanceResultMsg generateTimeoutResponse(QueryBalanceRequestMsg request)	{
    	
    	// generate a new response message
    	QueryBalanceResultMsg response = new QueryBalanceResultMsg();
    	ResultHeader responseHeader = new ResultHeader();
    	
    	// set the details in the response message
    	responseHeader.setResultCode(code);
    	responseHeader.setResultDesc(message);
    	responseHeader.setTransactionId(request.getRequestHeader().getTransactionId());
    	responseHeader.setCommandId(request.getRequestHeader().getCommandId());
    	responseHeader.setVersion(request.getRequestHeader().getVersion());
    	responseHeader.setSequenceId(request.getRequestHeader().getSequenceId());
    	responseHeader.setThirdPartyID(request.getRequestHeader().getThirdPartyID());
    	
    	// set the header in the response message
    	response.setResultHeader(responseHeader);
    	
		return response;
    }

}
