package com.ppm.integration.agilesdk.connector.agm;

import java.util.Random;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.AgmConstants;

public class CommonParameters {

	private static ValueSet values = new ValueSet();
	
	public static ValueSet getDefaultValueSet(){
		return initValueSet();
	}
	
	private static ValueSet initValueSet(){
		values.put(AgmConstants.KEY_BASE_URL,
				"http://myd-vm03471.hpswlabs.adapps.hp.com:8080");
		values.put(AgmConstants.KEY_USERNAME, "sa");
		values.put(AgmConstants.KEY_PASSWORD, "1qaz2wsx");
		values.put(AgmConstants.KEY_WORKSPACE, "1000");
		values.put(AgmConstants.KEY_PROJECT, "Main");
		values.put(AgmConstants.KEY_DOMAIN, "t1_sa");
		values.put(AgmConstants.KEY_RELEASE, "1032");
		values.put(AgmConstants.KEY_PROXY_HOST, "web-proxy.sgp.hp.com");
		values.put(AgmConstants.KEY_PROXY_PORT, "8080");

		values.put(AgmConstants.APP_CLIENT_ID, "api_client_1_8");
		values.put(AgmConstants.APP_CLIENT_SECRET, "8M1eLIeTeHs7NiG");
		return values;
	}
	

	public static String genRandomNumber(int digitNumber) {
		StringBuilder num = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < digitNumber; i++) {
			num.append(random.nextInt(10));
		}
		return num.toString();
	}

}
