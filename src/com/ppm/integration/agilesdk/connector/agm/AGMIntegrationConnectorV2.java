package com.ppm.integration.agilesdk.connector.agm;

import java.util.Arrays;
import java.util.List;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.ui.*;

public class AGMIntegrationConnectorV2 extends AGMIntegrationConnector {

    @Override
    public String getExternalApplicationVersionIndication() {
        return "2.X";
    }

    @Override public String getConnectorVersion() {
        return "1.0";
    }

	@Override
	public List<Field> getDriverConfigurationFields() {
		return Arrays.asList(new Field[] {
				new PlainText(AgmConstants.KEY_BASE_URL, "BASE_URL", "https://agilemanager-int.saas.hp.com", "",true),
				new LineBreaker(),
				new PlainText(AgmConstants.KEY_PROXY_HOST,"PROXY_HOST","","",false),
				new PlainText(AgmConstants.KEY_PROXY_PORT,"PROXY_PORT","","",false),
				new CheckBox(AgmConstants.KEY_USE_GLOBAL_PROXY,"USE_GLOBAL_PROXY","",false),
				new LineBreaker(),
				new PlainText(AgmConstants.APP_CLIENT_ID, "CLIENT_ID", "","", true),
                new PasswordText(AgmConstants.APP_CLIENT_SECRET, "CLIENT_SECRET", "", "", true),
		});
	}

	@Override
	public List<FunctionIntegration> getIntegrations() {
		return Arrays.asList(new FunctionIntegration[]{
			new AGMWorkPlanIntegrationV2(),
			new AGMTimeSheetIntegrationV2()
		});
	}

}
