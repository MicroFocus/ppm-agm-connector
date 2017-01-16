package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.ui.Field;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static org.aspectj.lang.reflect.DeclareAnnotation.Kind.Field;

public class AGMIntegrationConnectorV2Test {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetDriverConfigurationFields() {
		AGMIntegrationConnectorV2 aic2 = new AGMIntegrationConnectorV2();
		List<Field> fields = aic2.getDriverConfigurationFields();
		Assert.assertNotNull(fields);
		int expectedSize = 8;
		Assert.assertEquals(expectedSize, fields.size());
	}

	@Test
	public void testGetIntegrations(){
		AGMIntegrationConnectorV2 aic2 = new AGMIntegrationConnectorV2();
		List<FunctionIntegration> is = aic2.getIntegrations();
		Assert.assertNotNull(is);
		int expectedSize = 2;
		Assert.assertEquals(expectedSize, is.size());
	}
}
