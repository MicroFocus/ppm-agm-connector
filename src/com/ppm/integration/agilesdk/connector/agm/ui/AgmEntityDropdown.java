package com.ppm.integration.agilesdk.connector.agm.ui;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.AGMConnectivityExceptionHandler;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.ui.DynamicDropdown;

import java.util.ArrayList;
import java.util.List;

public abstract class AgmEntityDropdown extends DynamicDropdown {

	private Client client;

	public AgmEntityDropdown(String name, String labelKey,String display, boolean isRequired) {
		super(name, labelKey, display,isRequired);
	}
	
	public AgmEntityDropdown(String name, String labelKey, boolean isRequired) {
		super(name, labelKey, null,isRequired);
	}

	@Override
	public abstract List<String> getDependencies();

	public abstract List<Option> fetchDynamicalOptions(ValueSet values);

	@Override
	public List<Option> getDynamicalOptions(ValueSet values) {
		try{
			return fetchDynamicalOptions(values);
		}catch(Throwable e){
			new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
			return new ArrayList<Option>(0);
		}
	}
	

}
