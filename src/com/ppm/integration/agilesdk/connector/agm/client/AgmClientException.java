package com.ppm.integration.agilesdk.connector.agm.client;

public class AgmClientException extends RuntimeException {

	private static final long serialVersionUID = -8675720054664066527L;

	private final String errorCode;

	private final String msgKey;

	private final String[] params;

	public AgmClientException(String code, String msgKey, String... params){
		this.errorCode = code;
		this.msgKey = msgKey;
		this.params = params;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMsgKey() {
		return msgKey;
	}

	public String[] getParams() {
		return params;
	}
}
