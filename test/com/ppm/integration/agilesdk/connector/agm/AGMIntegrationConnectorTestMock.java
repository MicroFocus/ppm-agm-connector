package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.ui.Field;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class AGMIntegrationConnectorTestMock {

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testGetTargetApplication(){
		AGMIntegrationConnector aic = new AGMIntegrationConnector();
		String str=aic.getExternalApplicationName();
		Assert.assertNotNull(str);
	}	
	
	@Test
	public void testGetTargetApplicationVersion(){
		AGMIntegrationConnector aic = new AGMIntegrationConnector();
		String str=aic.getExternalApplicationVersionIndication();
		Assert.assertNotNull(str);
	}
	
	@Test
	public void testGetTargetApplicationIcon(){
		AGMIntegrationConnector aic = new AGMIntegrationConnector();
		String str=aic.getTargetApplicationIcon();
		Assert.assertNotNull(str);
	}	
	
	@Test
	public void testGetDriverConfigurationFields() {
		AGMIntegrationConnector aic = new AGMIntegrationConnector();
		List<Field> fields = aic.getDriverConfigurationFields();
		Assert.assertNotNull(fields);
		int expectedSize = 5;
		Assert.assertEquals(expectedSize, fields.size());
	}

	@Test
	public void testGetIntegrations(){
		AGMIntegrationConnector aic = new AGMIntegrationConnector();
		List<FunctionIntegration> fis = aic.getIntegrations();
		Assert.assertNotNull(fis);
		int expectedSize = 2;
		Assert.assertEquals(expectedSize, fis.size());
	}
	
	@Test
	public void testGetBuildinInstance() {
		AGMIntegrationConnector aic = new AGMIntegrationConnector();		
		Assert.assertNull(aic.getBuiltInDefaultInstance());
	}
}
