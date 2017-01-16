package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;
import java.util.Random;

import com.ppm.integration.agilesdk.ValueSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ppm.integration.agilesdk.connector.agm.client.publicapi.AccessToken;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ReleaseEntity;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.SprintDurationUnitsEnum;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.TimesheetItem;

/**
 * Created by libingc on 3/9/2016.
 */
public class ClientPublicAPITest {

	ValueSet values = new ValueSet();

	@Before
	public void setUp() {
        values = CommonParameters.getDefaultValueSet();

	}


	private String genRandomNumber(int digitNumber) {
		StringBuilder num = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < digitNumber; i++) {
			num.append(random.nextInt(10));
		}
		return num.toString();
	}

	@Test
	public void testCreateReleaseInWorkspace() throws Exception {
		ClientPublicAPI client = new AGMClientUtils()
				.setupClientPublicAPI(values);

		String clientId = values.get(AgmConstants.APP_CLIENT_ID);
		String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
		AccessToken token = client.getAccessTokenWithFormFormat(clientId,
				clientSecret);
		int workspaceId = 1000;
		
//		String xmString = new String("����".toString().getBytes("UTF-8"));  
//		xmString = URLEncoder.encode(xmString, "UTF-8");
		String xmString = "AutoCreate";
		String releaseName = xmString + genRandomNumber(4);
		String releaseDesc = "Create release UT;";
		String startDate = "2016-04-16";
		String endDate = "2016-05-30";
		int sprintDuration = 3;
		SprintDurationUnitsEnum sprintUnit = SprintDurationUnitsEnum.DAYS;
		ReleaseEntity releaseEntity = client.createReleaseInWorkspace(token,
				workspaceId, releaseName, releaseDesc, startDate, endDate,
				sprintDuration, sprintUnit);
		Assert.assertNotNull(releaseEntity);
	}

	@Test
	public void testGetAccessTokenWithFormFormat() throws Exception {
		values.put(AgmConstants.KEY_PROXY_HOST, "web-proxy.sgp.hp.com");
		values.put(AgmConstants.KEY_PROXY_PORT, "8080");
		ClientPublicAPI client = new AGMClientUtils()
				.setupClientPublicAPI(values);

		String clientId = values.get(AgmConstants.APP_CLIENT_ID);
		String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
		AccessToken token = client.getAccessTokenWithFormFormat(clientId,
				clientSecret);
		Assert.assertNotNull(token);
		System.out.println(token);
	}

	@Test
	public void testURLWithSlash() throws Exception {
		values.put(AgmConstants.KEY_BASE_URL, values.get(AgmConstants.KEY_BASE_URL)+"/");
		ClientPublicAPI client = new AGMClientUtils()
				.setupClientPublicAPI(values);

		String clientId = values.get(AgmConstants.APP_CLIENT_ID);
		String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
		AccessToken token = client.getAccessTokenWithFormFormat(clientId,
				clientSecret);
		Assert.assertNotNull(token);
		System.out.println(token);
	}
	
	@Test
	public void testSetProxy() throws Exception {
		ClientPublicAPI client = new AGMClientUtils()
				.setupClientPublicAPI(values);
		String host = "web-proxy.sgp.hp.com"; 
		int port = 8080;
		client.setProxy(host, port);
		String clientId = values.get(AgmConstants.APP_CLIENT_ID);
		String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
		AccessToken token = client.getAccessTokenWithFormFormat(clientId,
				clientSecret);
		Assert.assertNotNull(token);
		System.out.println(token);
	}

	@Test
	public void testGetReleaseData() throws Exception {
		ClientPublicAPI client = new AGMClientUtils()
				.setupClientPublicAPI(values);

		String userName = "sa";
		String startDateStr = "2016-03-31";
		String endDateStr = "2016-04-30";
		int workspaceId = 1000;

		String clientId = values.get(AgmConstants.APP_CLIENT_ID);
		String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
		AccessToken token = client.getAccessTokenWithFormFormat(clientId,
				clientSecret);

		List<TimesheetItem> timeSheets = client.getTimeSheetData(token,
				userName, startDateStr, endDateStr, workspaceId);
		for (TimesheetItem item : timeSheets) {
			System.out.println(item);
		}
	}

}