package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.ppm.integration.agilesdk.connector.agm.AGMIntegrationConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.ui.Field;

public class AGMIntegrationConnectorTest {

	@Before
	public void setUp() throws Exception {
		
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
}
