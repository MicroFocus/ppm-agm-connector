package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.IntegrationException;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import org.apache.wink.client.ClientRuntimeException;

import java.lang.Thread.UncaughtExceptionHandler;

public class AGMConnectivityExceptionHandler implements
		UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		uncaughtException(t,  e, AGMIntegrationConnector.class);
	}

	public void uncaughtException(Thread t, Throwable e, Class cls) {
		if(e instanceof ClientRuntimeException){
			handleClientRuntimeException((ClientRuntimeException)e, cls);
		}else if( e instanceof AgmClientException){
			handleAgmClientException((AgmClientException) e, cls);
		}else{
			throw IntegrationException.build(cls)
					.setErrorCode("PPM_INT_AGM_ERR_202")
					.setMessage("ERROR_UNKNOWN_ERROR",e.getMessage());
		}
	}

	private void handleAgmClientException(AgmClientException e, Class cls){
		throw IntegrationException.build(cls)
				.setErrorCode(e.getErrorCode())
				.setMessage(e.getMsgKey(), e.getParams());
	}

	private void handleClientRuntimeException( ClientRuntimeException e , Class cls){
		java.net.UnknownHostException unknownHost = extractException(e, java.net.UnknownHostException.class);
		if(unknownHost!=null){
			throw IntegrationException.build(cls)
					.setErrorCode("PPM_INT_AGM_ERR_202")
					.setMessage("ERROR_UNKNOWN_HOST_ERROR",unknownHost.getMessage());
		}

		java.net.ConnectException connectException = extractException(e, java.net.ConnectException.class);
		if(connectException!=null){
			throw IntegrationException.build(cls)
					.setErrorCode("PPM_INT_AGM_ERR_202")
					.setMessage("ERROR_CONNECTIVITY_ERROR");
		}

		throw IntegrationException.build(cls)
				.setErrorCode("PPM_INT_AGM_ERR_202")
				.setMessage("ERROR_CONNECTIVITY_ERROR");
	}

	@SuppressWarnings("unchecked")
	protected <T extends Throwable> T extractException(ClientRuntimeException e, Class<T> clazz){

		Throwable t = e;
		while( !clazz.isInstance(t) && t != null ){
			t = t.getCause();
		}

		return (T) t;
	}
}
