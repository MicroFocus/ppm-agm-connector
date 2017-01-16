package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.ppm.integration.agilesdk.connector.agm.AGMIntegrationConnectorV2;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.ui.Field;
import com.hp.ppm.user.service.api.UserService;
import com.mercury.itg.core.impl.SpringContainerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringContainerFactory.class})
@PowerMockIgnore("javax.management.*")
public class AGMIntegrationConnectorV2TestMock {

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testGetTargetApplicationVersion(){
		AGMIntegrationConnectorV2 aic2 = new AGMIntegrationConnectorV2();
		String str=aic2.getExternalApplicationVersionIndication();
		Assert.assertNotNull(str);
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
		
		UserService userService=PowerMockito.mock(UserService.class);
		PowerMockito.mockStatic(SpringContainerFactory.class);
		PowerMockito.when(SpringContainerFactory.getBean("userAdminService")).thenReturn(userService);
	
		List<FunctionIntegration> is = aic2.getIntegrations();
		Assert.assertNotNull(is);
		int expectedSize = 2;
		Assert.assertEquals(expectedSize, is.size());
	}
}
