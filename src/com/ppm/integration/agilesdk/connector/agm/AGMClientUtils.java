package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.provider.Providers;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AGMClientUtils {

    private final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

    protected String getLiteralDate(Date d){
        return dateformat.format(d);
    }

    public Client setupClient(Client client, List<String> cookies, ValueSet values){
        String proxyHost = null, proxyPort = null;
        if(values.getBoolean(AgmConstants.KEY_USE_GLOBAL_PROXY, false)){

            String proxyURL = Providers.getServerConfigurationProvider(AGMIntegrationConnector.class).getServerProperty("HTTP_PROXY_URL");
            Matcher m = Pattern.compile("^([^:]*)(:(\\d+))?$").matcher(proxyURL);
            if(m.matches()){
                proxyHost = m.group(1);
                proxyPort = m.group(3);
                proxyPort = proxyPort == null? "80":proxyPort;
            }
        }else{
            proxyHost = values.get(AgmConstants.KEY_PROXY_HOST);
            proxyPort = values.get(AgmConstants.KEY_PROXY_PORT);
        }

        if(!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort) && StringUtils.isNumeric(proxyPort)) {
            Logger.getLogger(AGMWorkPlanIntegration.class).debug(String.format("Use HTTP Proxy HOST=%s PORT=%s", proxyHost, proxyPort));
            client.proxy(proxyHost, (int) Long.parseLong(proxyPort));
        }else{
            if(!StringUtils.isEmpty(proxyHost) || !StringUtils.isEmpty(proxyPort)) {
                Logger.getLogger(AGMWorkPlanIntegration.class).error(String.format("Invalid HTTP Proxy HOST=%s PORT=%s", proxyHost, proxyPort));
            }
        }

        if(cookies!=null && cookies.size() > 0){
            client.auth(cookies);
        }else{
            client.auth(values.get(AgmConstants.KEY_USERNAME),values.get(AgmConstants.KEY_PASSWORD));
        }
        return client;
    }

    public Client setupClient(Client client, ValueSet values){
        return setupClient(client,null,values);
    }

    public ClientPublicAPI setupClientPublicAPI(ValueSet values) {
        ClientPublicAPI client = new ClientPublicAPI(values.get(AgmConstants.KEY_BASE_URL));
        String proxyHost = null, proxyPort = null;
        if(values.getBoolean(AgmConstants.KEY_USE_GLOBAL_PROXY, false)){

            String proxyURL = Providers.getServerConfigurationProvider(AGMIntegrationConnector.class).getServerProperty("HTTP_PROXY_URL");
            Matcher m = Pattern.compile("^([^:]*)(:(\\d+))?$").matcher(proxyURL);
            if(m.matches()){
                proxyHost = m.group(1);
                proxyPort = m.group(3);
                proxyPort = proxyPort == null? "80":proxyPort;
            }
        }else{
            proxyHost = values.get(AgmConstants.KEY_PROXY_HOST);
            proxyPort = values.get(AgmConstants.KEY_PROXY_PORT);
        }

        if(!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort) && StringUtils.isNumeric(proxyPort)) {
            Logger.getLogger(AGMWorkPlanIntegration.class).debug(String.format("Use HTTP Proxy HOST=%s PORT=%s", proxyHost, proxyPort));
            client.setProxy(proxyHost, (int) Long.parseLong(proxyPort));
        }else{
            if(!StringUtils.isEmpty(proxyHost) || !StringUtils.isEmpty(proxyPort)) {
                Logger.getLogger(AGMWorkPlanIntegration.class).error(String.format("Invalid HTTP Proxy HOST=%s PORT=%s", proxyHost, proxyPort));
            }
        }

        return client;
    }
}
