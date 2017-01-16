package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.FunctionIntegration;
import com.ppm.integration.agilesdk.IntegrationConnector;
import com.ppm.integration.agilesdk.IntegrationConnectorInstance;
import com.ppm.integration.agilesdk.ui.CheckBox;
import com.ppm.integration.agilesdk.ui.Field;
import com.ppm.integration.agilesdk.ui.LineBreaker;
import com.ppm.integration.agilesdk.ui.PlainText;
import com.kintana.core.util.LocaleUtil;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AGMIntegrationConnector extends IntegrationConnector {

    private static final String AGM_RESOURCE_BUNDLE_NAME = "com.ppm.integration.agilesdk.connector.agm.AGMIntegrationConnector";

    @Override
    public String getExternalApplicationName() {
        return "Agile Manager";
    }

    @Override
    public String getExternalApplicationVersionIndication() {
            ResourceBundle bundle =
                ResourceBundle.getBundle(AGM_RESOURCE_BUNDLE_NAME,
                        LocaleUtil.getLanguageLocale());
        String deprecated =  bundle.getString("DEPRECATED");
        return "1.X ("+deprecated+")";
    }

    @Override public String getConnectorVersion() {
        return "1.0";
    }

    @Override
    public String getTargetApplicationIcon() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABTElEQVQ4jdXTsUvVYRTG8c/79gvLa1f7XUhykaLwDhY0GA2CVJAt6dwfUCANNbWEpu4RQeLcIvgPBLmVSEtLS0KFIEJxScOi6wWx+2t5L5U0CC72wIHzwvk+nPPAG4qisB/FfdEHwiCbvz0HZ/AIQ9jE85d5Pj3b2zuNm6jH0Hxa3754LChuJfYZ7mc4jTfoxDbKGNv8uXWu43vt0o9yd0DH+dLbkYVKtRj+vNyFHdzD14iHCZ5HF04UvFg8Gi+0Nb5lsblzCoNX84W+5c7uKvpxNm0xFnEjPe6igfUPbYceNGIowfEvK2uV2vul/PBGVgjlx9XL77CamJMR7dhCrRXMk8qRj7vDiprt/wox4lUymUBPOmnPyhIwhKlUZj7VIcCdnlJrNuxiQ8vgNa5gHAPI/xxenxxN3ehv9NrfG8ASrqd+MtWeFP7/z/QLEElRNu+xnv8AAAAASUVORK5CYII=";
    }

    @Override
    public List<Field> getDriverConfigurationFields() {
        return Arrays.asList(new Field[] {
            new PlainText(AgmConstants.KEY_BASE_URL, "BASE_URL", "https://agilemanager-int.saas.hp.com", "block",true),
            new LineBreaker(),
            new PlainText(AgmConstants.KEY_PROXY_HOST,"PROXY_HOST","","block",false),
            new PlainText(AgmConstants.KEY_PROXY_PORT,"PROXY_PORT","","block",false),
            new CheckBox(AgmConstants.KEY_USE_GLOBAL_PROXY,"USE_GLOBAL_PROXY","block",false)
        });
    }

    @Override
    public List<FunctionIntegration> getIntegrations() {
        return Arrays.asList(new FunctionIntegration[]{
            new AGMWorkPlanIntegration(),
            new AGMTimeSheetIntegration()
        });
    }
}
