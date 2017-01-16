package com.ppm.integration.agilesdk.connector.agm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ReleaseEntity;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.ppm.integration.agilesdk.pm.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientWebException;

import com.ppm.integration.agilesdk.ValueSet;
import com.hp.ppm.integration.model.WorkplanMapping;
import com.ppm.integration.agilesdk.provider.Providers;
import com.ppm.integration.agilesdk.provider.UserProvider;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.AccessToken;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.SprintDurationUnitsEnum;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.Projects;
import com.ppm.integration.agilesdk.ui.CheckBox;
import com.ppm.integration.agilesdk.ui.DatePicker;
import com.ppm.integration.agilesdk.ui.Field;
import com.ppm.integration.agilesdk.ui.FieldAppearance;
import com.ppm.integration.agilesdk.ui.LabelText;
import com.ppm.integration.agilesdk.ui.LineBreaker;
import com.ppm.integration.agilesdk.ui.LineHr;
import com.ppm.integration.agilesdk.ui.NumberText;
import com.ppm.integration.agilesdk.ui.PasswordText;
import com.ppm.integration.agilesdk.ui.PlainText;
import com.hp.ppm.user.model.User;
import com.hp.ppm.user.service.api.UserService;
import com.kintana.core.util.LocaleUtil;
import com.kintana.core.util.mlu.DateFormatter;
import com.mercury.itg.core.ContextFactory;
import com.mercury.itg.core.impl.SpringContainerFactory;
import com.mercury.itg.core.model.Context;
import com.mercury.itg.core.user.impl.UserImpl;

public class AGMWorkPlanIntegrationV2 extends WorkPlanIntegration {

    protected AGMClientUtils clientUtils = new AGMClientUtils();

    private static final String AGM_V2_RESOURCE_BUNDLE_NAME = "com.ppm.integration.agilesdk.connector.agm.AGMIntegrationConnectorV2";

    private final Logger logger = Logger.getLogger(this.getClass());
    protected static final UserService userService = ((UserService)SpringContainerFactory.getBean("userAdminService"));
    private String getUserDateformt(){
        try {
            Context ctx = ContextFactory.getThreadContext();
            UserImpl currentUser = (UserImpl)ctx.get(Context.USER);
            Long userID=currentUser.getUserId();
            if(userID!=null){
                return userService.findUserRegionalById(userID.intValue()).getShortDateFormat();
            }
        } catch (Exception e) {
            logger.error("Create new Release getUserDateformt fail:", e);
        }
        return null;
    }
    
    protected Client getClient(ValueSet values){
        Client client = new Client(values.get(AgmConstants.KEY_BASE_URL));
        clientUtils.setupClient(client, values);
        return client;
    }

    private static String[] ignoreCreateReleaseSettingInDisplayConfigMapping = new String[] {
            "IS_CREATE_RELEASE",
            "CREATE_RELEASE_NAME",
            "CREATE_RELEASE_DESCRIPTION",
            "CREATE_RELEASE_START_TIME",
            "CREATE_RELEASE_END_TIME",
            "CREATE_RELEASE_SPRINT_DURATION",
            "CREATE_RELEASE_SPRINT_DURATION_UNIT",
    };

    private static final String RELEASE_G11N_KEY_IN_DESPLAY_CONFIG_JSON = "RELEASE";

    private String getMessage(String key) {
        ResourceBundle bundle =
                ResourceBundle.getBundle(AGM_V2_RESOURCE_BUNDLE_NAME,
                        LocaleUtil.getLanguageLocale());
        return bundle.getString(key);
    }
    @Override
    public List<Field> getMappingConfigurationFields(WorkPlanIntegrationContext context2,ValueSet values) {
        GregorianCalendar start = context2.currentTask().getSchedule().getScheduledStart().toGregorianCalendar();
        GregorianCalendar finish = context2.currentTask().getSchedule().getScheduledEnd().toGregorianCalendar();

        return Arrays.asList(new Field[]{
            new PlainText(AgmConstants.KEY_USERNAME,"USERNAME","","block",true),
            new PasswordText(AgmConstants.KEY_PASSWORD,"PASSWORD","","block",true),
            new LineBreaker(),
            new AgmEntityDropdown(AgmConstants.KEY_DOMAIN,"DOMAIN","block",true){

                @Override
                public List<String> getDependencies(){
                    return Arrays.asList(new String[]{
                            AgmConstants.KEY_BASE_URL,
                            AgmConstants.KEY_PROXY_HOST,
                            AgmConstants.KEY_PROXY_PORT,
                            AgmConstants.KEY_USERNAME,
                            AgmConstants.KEY_PASSWORD
                    });
                }

                @Override
                public List<Option> fetchDynamicalOptions(ValueSet values) {
                    Client client = getClient(values);

                    if(!values.isAllSet(AgmConstants.KEY_BASE_URL, AgmConstants.KEY_USERNAME, AgmConstants.KEY_PASSWORD)){
                        return null;
                    }

                    List<Domain> domains = client.getDomains();

                    List<Option> options = new ArrayList<Option>(domains.size());
                    for(Domain d : domains){
                        options.add(new Option(d.name,d.name));
                    }
                    return options;
                }
            },
            new AgmEntityDropdown(AgmConstants.KEY_PROJECT,"PROJECT","block",true){

                @Override
                public List<String> getDependencies(){
                    return Arrays.asList(new String[]{AgmConstants.KEY_DOMAIN});
                }

                @Override
                public List<Option> fetchDynamicalOptions(ValueSet values) {
                    Client client = getClient(values);

                    if(!values.isAllSet(AgmConstants.KEY_BASE_URL, AgmConstants.KEY_USERNAME, AgmConstants.KEY_PASSWORD, AgmConstants.KEY_DOMAIN)){
                        return null;
                    }

                    Projects projects = client.getProjects(values.get(AgmConstants.KEY_DOMAIN));
                    List<Option> options = new ArrayList<Option>(projects.getCollection().size());
                    for(Project p : projects.getCollection()){
                        options.add(new Option(p.name,p.name));
                    }
                    return options;
                }
            },
            new AgmEntityDropdown(AgmConstants.KEY_WORKSPACE,"AGM_WORKSPACE","block",true){

                @Override
                public List<String> getDependencies(){
                    return Arrays.asList(new String[]{AgmConstants.KEY_PROJECT});
                }

                @Override
                public List<Option> fetchDynamicalOptions(ValueSet values) {
                    Client client = getClient(values);

                    if(!values.isAllSet(AgmConstants.KEY_PROJECT)){
                        return null;
                    }

                    Entities workspaces = client.getWorkSpaces(values.get(AgmConstants.KEY_DOMAIN), values.get(AgmConstants.KEY_PROJECT));

                    List<Option> options = new ArrayList<Option>(workspaces.getEntity().size());

                    for(EntityComplexType e : workspaces.getEntity()){
                        EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
                        if (reader.intValue("id", 0) > 0) {
                            options.add(new Option(reader.strValue("id"),reader.strValue("name")));
                        }
                    }
                    return options;
                }
            },
            new LineHr(),
            new AgmEntityDropdown(AgmConstants.KEY_RELEASE,"RELEASE","block",true){

                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {

                    FieldAppearance option = new FieldAppearance();
                    if (!values.isAllSet(AgmConstants.KEY_CREATE_RELEASE)) {
                        return null;
                    }

                    String isCreateRelease = values.get(AgmConstants.KEY_CREATE_RELEASE);
                    if (isCreateRelease.equals("false")) {

                        option = new FieldAppearance("required", "disabled");

                    } else if (isCreateRelease.equals("true")) {

                        option = new FieldAppearance("disabled", "required");
                    }

                    return option;
                }


                @Override
                public List<String> getDependencies(){
                    return Arrays.asList(new String[]{AgmConstants.KEY_WORKSPACE});
                }

                @Override
                public List<Option> fetchDynamicalOptions(ValueSet values) {
                    Client client = getClient(values);

                    if(!values.isAllSet(AgmConstants.KEY_WORKSPACE)){
                        return null;
                    }
                    Entities releases =client.getReleases(values.get(AgmConstants.KEY_DOMAIN), values.get(AgmConstants.KEY_PROJECT), values.get(AgmConstants.KEY_WORKSPACE));

                    List<Option> options = new ArrayList<Option>(releases.getEntity().size());

                    for(EntityComplexType e : releases.getEntity()){
                        EntityComplexTypeReader reader = new EntityComplexTypeReader(e);

                        options.add(new Option(reader.strValue("id"),reader.strValue("name")));
                    }
                    return options;
                }
                },
                new AgmEntityDropdown(AgmConstants.KEY_SPRINT, "SPRINT", "block", false) {

                    @Override
                    public List<String> getStyleDependencies() {
                        return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                    }

                    @Override
                    public FieldAppearance getFieldAppearance(ValueSet values) {
                        return getCreateReleaseUselessFieldsAppearance(values);
                    }

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {AgmConstants.KEY_RELEASE});
                    }

                    @Override
                    public List<Option> fetchDynamicalOptions(ValueSet values) {
                        Client client = getClient(values);

                        if (!values.isAllSet(AgmConstants.KEY_WORKSPACE, AgmConstants.KEY_PROJECT,
                                AgmConstants.KEY_RELEASE)) {
                            return null;
                        }
                        Entities sprints =
                                client.getSprintsByParentId(values.get(AgmConstants.KEY_DOMAIN),
                                        values.get(AgmConstants.KEY_PROJECT),
                                        values.get(AgmConstants.KEY_RELEASE));

                        List<Option> options = new ArrayList<Option>(sprints.getEntity().size());

                        for (EntityComplexType e : sprints.getEntity()) {
                            EntityComplexTypeReader reader = new EntityComplexTypeReader(e);

                            options.add(new Option(reader.strValue("id"), reader.strValue("name")));
                        }
                        return options;
                    }
                }

            ,new LineBreaker()
,
                new AgmEntityDropdown(AgmConstants.KEY_DATA_DETAIL_LEVEL, "DATA_DETAIL_LEVEL",
                        "block", false) {

                    @Override
                    public List<String> getStyleDependencies() {
                        return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                    }

                    @Override
                    public FieldAppearance getFieldAppearance(ValueSet values) {
                            return getCreateReleaseUselessFieldsAppearance(values);
                    }

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {AgmConstants.KEY_RELEASE, AgmConstants.KEY_SPRINT});
                    }

                    @Override
                    public List<Option> fetchDynamicalOptions(ValueSet values) {
                        
                        if (!values.isAllSet(AgmConstants.KEY_RELEASE)) {
                            return null;
                        }

                        List<Option> options = new ArrayList<Option>(3);

                        
                        if (StringUtils.isBlank(values.get(AgmConstants.KEY_SPRINT))) {
                            // Release level options
                            options.add(new Option(AgmConstants.DETAILS_ALL,
                                    getMessage("ALL_LEVEL_RELEASE")));
                            options.add(new Option(AgmConstants.DETAILS_SPRINT,
                                    getMessage("ONLY_RELEASE_SPRINT_LEVEL_RELEASE")));
                            options.add(new Option(AgmConstants.DETAILS_RELEASE,
                                    getMessage("ONLY_RELEASE_LEVEL_RELEASE")));
                        } else {
                            // Sprint level options
                            options.add(new Option(AgmConstants.DETAILS_ALL,
                                    getMessage("ALL_LEVEL_SPRINT")));
                            options.add(new Option(AgmConstants.DETAILS_SPRINT,
                                    getMessage("ONLY_SPRINT_LEVEL_SPRINT")));
                        }


                        return options;
                    }
                    

                },
            //Create new release field
            new LineBreaker(),
            new CheckBox(AgmConstants.KEY_CREATE_RELEASE,"IS_CREATE_RELEASE","block",false),
            new LineBreaker(),
            new PlainText(AgmConstants.KEY_NAME,"CREATE_RELEASE_NAME","","block",false)
            {
                    @Override
                    public List<String> getStyleDependencies() {
                        return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                    }

                    @Override
                    public FieldAppearance getFieldAppearance(ValueSet values) {
                        return getCreateReleaseFieldsAppearance(values);

                    }

            },
            new PlainText(AgmConstants.KEY_DESCRIPTION,"CREATE_RELEASE_DESCRIPTION","","block",false)
            {
                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {
                    return getCreateReleaseNotRequiredFieldsAppearance(values);

                }
            },
            new LineBreaker(),
            new DatePicker(AgmConstants.KEY_START_TIME,"CREATE_RELEASE_START_TIME",new SimpleDateFormat(getUserDateformt()).format(start.getTime()),"block",false)
            {
                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {
                    return getCreateReleaseFieldsAppearance(values);

                }
            },
            new DatePicker(AgmConstants.KEY_END_TIME,"CREATE_RELEASE_END_TIME",new SimpleDateFormat(getUserDateformt()).format(finish.getTime()),"block",false)
            {
                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {
                    return getCreateReleaseFieldsAppearance(values);

                }
            },
            new NumberText(AgmConstants.KEY_SPRINT_DURATION,"CREATE_RELEASE_SPRINT_DURATION","","block",false)
            {
                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {
                    return getCreateReleaseFieldsAppearance(values);

                }
            },
            new AgmEntityDropdown(AgmConstants.KEY_SRRINT_DURATION_UNIT,"CREATE_RELEASE_SPRINT_DURATION_UNIT","block",false){

                @Override
                public List<String> getStyleDependencies() {
                    return Arrays.asList(new String[] { AgmConstants.KEY_CREATE_RELEASE });
                }

                @Override
                public FieldAppearance getFieldAppearance(ValueSet values) {
                    return getCreateReleaseFieldsAppearance(values);

                }

                @Override
                public List<String> getDependencies(){
                    return Arrays.asList(new String[]{AgmConstants.KEY_NAME});
                }

                @Override
                public List<Option> fetchDynamicalOptions(ValueSet values) {

                    List<Option> options = new ArrayList<Option>(2);
                    Option optDay=new Option(SprintDurationUnitsEnum.DAYS.getText(), getMessage("CREATE_RELEASE_DAY"));
                    Option optWeek=new Option(SprintDurationUnitsEnum.WEEKS.getText(),getMessage("CREATE_RELEASE_WEEK"));
                    options.add(optDay);
                    options.add(optWeek);
                    return options;
                }
            }
            ,new LineBreaker()
            ,new LabelText("", "SHOW_RELEASE_INFORMATION","block",false)
            ,new LineBreaker()
            ,new CheckBox(AgmConstants.KEY_SHOW_SPRINT_BURN_DOWN,"SHOW_SPRINT_BURN_DOWN","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_RELEASE_BURN_UP,"SHOW_RELEASE_BURN_UP","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_THEME_STATUS,"SHOW_THEME_STATUS","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_FEATURE_STATUS,"SHOW_FEATURE_STATUS","block",true)
        });
    }
    
    public FieldAppearance getCreateReleaseFieldsAppearance(ValueSet values)
    {
        FieldAppearance option = new FieldAppearance();
        if (!values.isAllSet(AgmConstants.KEY_CREATE_RELEASE)) {
            return null;
        }

        String isCreateRelease = values.get(AgmConstants.KEY_CREATE_RELEASE);
        if (isCreateRelease.equals("false")) {

            option = new FieldAppearance("disabled", "required");

        } else if (isCreateRelease.equals("true")) {

            option = new FieldAppearance("required", "disabled");
        }

        return option;
    }
    
    public FieldAppearance getCreateReleaseNotRequiredFieldsAppearance(ValueSet values)
    {
        FieldAppearance option = new FieldAppearance();
        if (!values.isAllSet(AgmConstants.KEY_CREATE_RELEASE)) {
            return null;
        }

        String isCreateRelease = values.get(AgmConstants.KEY_CREATE_RELEASE);
        if (isCreateRelease.equals("false")) {

            option = new FieldAppearance("disabled", "");

        } else if (isCreateRelease.equals("true")) {

            option = new FieldAppearance("", "disabled");
        }

        return option;
    }
    
    public FieldAppearance getCreateReleaseUselessFieldsAppearance(ValueSet values)
    {
        FieldAppearance option = new FieldAppearance();
        if (!values.isAllSet(AgmConstants.KEY_CREATE_RELEASE)) {
            return null;
        }

        String isCreateRelease = values.get(AgmConstants.KEY_CREATE_RELEASE);
        if (isCreateRelease.equals("false")) {

            option = new FieldAppearance("", "disabled");

        } else if (isCreateRelease.equals("true")) {

            option = new FieldAppearance("disabled", "");
        }

        return option;
    }

    public void checkClientIdSet(String clientId , String clientSecret) {
        if( null == clientId || clientId.trim().length() == 0
                || null == clientSecret || clientSecret.trim().length() == 0 ) {
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), new AgmClientException("AGM_APP", "CLIENT_ID_OR_SECRET_KEY_NOT_SET"), AGMIntegrationConnectorV2.class);
        }
    }

    @Override
    public WorkplanMapping linkTaskWithExternal(WorkPlanIntegrationContext context, WorkplanMapping workplanMapping, ValueSet values) {
        boolean isCreateRelease = values.getBoolean(AgmConstants.KEY_CREATE_RELEASE, false);
        String clientId = values.get(AgmConstants.APP_CLIENT_ID);
        String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);

        checkClientIdSet(clientId, clientSecret);
        try {
            ClientPublicAPI client = clientUtils.setupClientPublicAPI(values);
            client.getAccessTokenWithFormFormat(clientId, clientSecret);
        }catch(Exception e) {
            logger.error("Error when access AGM with client id and secret key :", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e, AGMIntegrationConnectorV2.class);
        }

        if(isCreateRelease) {
            Integer newReleaseId = createNewRelease(values, clientId, clientSecret);
            //update mapping Release in ConfigJson
            String newConfigJson = updateNewReleaseInConfigJson(workplanMapping.getConfigJson(), String.valueOf(newReleaseId));
            workplanMapping.setConfigJson(newConfigJson);

            //update mapping Release in ConfigDisplayJson
            String releaseName = values.get(AgmConstants.KEY_NAME);
            String newDisplayconfigJson = updateNewReleaseInConfigDisplayJson(workplanMapping.getConfigDisplayJson(), releaseName);
            workplanMapping.setConfigDisplayJson(newDisplayconfigJson);
        }

        String newDisplayconfigJson = removeCreateReleaseInfoInConfigDisplayJson(workplanMapping.getConfigDisplayJson());
        workplanMapping.setConfigDisplayJson(newDisplayconfigJson);

        return workplanMapping;
    }


    public Integer  createNewRelease(ValueSet values, String clientId, String clientSecret) {
        try {
            logger.debug("create new release.");
            final Client authClient = new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL));
            //the auth is in this method: setupClient(authClient,null, values);
            clientUtils.setupClient(authClient, null, values);

            ClientPublicAPI client = clientUtils.setupClientPublicAPI(values);
            logger.debug("auth for clientid");
            int workspaceId = 0;
            try {
                workspaceId = Integer.parseInt(values.get(AgmConstants.KEY_WORKSPACE));
            } catch (Exception e) {
                throw new AgmClientException("AGM_APP", "ERROR_WORKSPACE_ID", e.getMessage());
            }

            String releaseName = values.get(AgmConstants.KEY_NAME);
            if(releaseName.length() >= 50 ) {
                throw new AgmClientException("AGM_APP", "ERROR_CREATE_RELEASE_NAME_LENGTH_SHOULD_LESS_THAN");
            }

            Date startDateD = DateFormatter.parseDateTime(values.get(AgmConstants.KEY_START_TIME));
            Date endDateD = DateFormatter.parseDateTime(values.get(AgmConstants.KEY_END_TIME));
            if(startDateD.after(endDateD)) {
                throw new AgmClientException("AGM_APP", "ERROR_CREATE_RELEASE_START_DATE_SHOULD_BEFORE_END_DATE");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String releaseDesc = values.get(AgmConstants.KEY_DESCRIPTION);
            String startDate = sdf.format(startDateD);
            String endDate = sdf.format(endDateD);
            int sprintDuration = values.getInteger(AgmConstants.KEY_SPRINT_DURATION, 2);
            String sprintDurationUnit = values.get(AgmConstants.KEY_SRRINT_DURATION_UNIT);
            SprintDurationUnitsEnum sprintUnit = SprintDurationUnitsEnum.fromText(sprintDurationUnit);
            logger.debug("get token start");
            AccessToken token = client.getAccessTokenWithFormFormat(clientId, clientSecret);
            logger.debug("get token done, and starting create release ");
            ReleaseEntity
                    release = client.createReleaseInWorkspace(token, workspaceId, releaseName, releaseDesc, startDate, endDate, sprintDuration, sprintUnit);
            return release.getId();
        } catch (Exception e) {
            logger.error("Create new Release fail:", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e, AGMIntegrationConnectorV2.class);
            return 0;
        }
    }


    /*
    * Update the configJson for mapping the new created release name
    * */
    public String updateNewReleaseInConfigJson(String configJson, String newReleaseId) {
        if(configJson == null) return configJson;
        JSONObject json = (JSONObject) JSONSerializer.toJSON(configJson);
        json.put(AgmConstants.KEY_RELEASE, newReleaseId);
        return json.toString();
    }

    /*
    * Update the configDisplayjson for mapping the new created release name
    * */
    public String updateNewReleaseInConfigDisplayJson(String configDisplayJson, String newReleaseName) {
        if(configDisplayJson == null) return configDisplayJson;
        JSONObject json = (JSONObject) JSONSerializer.toJSON(configDisplayJson);
        boolean foundMappingRelease = false;
        JSONArray configs = json.getJSONArray("config");
        for (int i = 0; i < configs.size(); i++) {
            JSONObject item = (JSONObject) configs.get(i);
            //Update the configDisplayjson for mapping the new created release name
            if (RELEASE_G11N_KEY_IN_DESPLAY_CONFIG_JSON.equals(item.get("label"))) {
                foundMappingRelease = true;
                item.put("text", newReleaseName);
            }
        }
        if (!foundMappingRelease) {
            // no release mapping item
            JSONObject item = new JSONObject();
            item.put("label", RELEASE_G11N_KEY_IN_DESPLAY_CONFIG_JSON);
            item.put("text", newReleaseName);
            configs.add(item);
        }
        return json.toString();
    }

    public String removeCreateReleaseInfoInConfigDisplayJson(String configDisplayJson) {
        if(configDisplayJson == null) return configDisplayJson;
        JSONObject json = (JSONObject) JSONSerializer.toJSON(configDisplayJson);
        JSONArray newConfig = new JSONArray();
        JSONArray configs = json.getJSONArray("config");

        List<String> containsNewReleaseMapping = Arrays.asList(ignoreCreateReleaseSettingInDisplayConfigMapping);
        for (int i = 0; i < configs.size(); i++) {
            JSONObject item = (JSONObject) configs.get(i);
            if (!containsNewReleaseMapping.contains(item.get("label"))) {
                newConfig.add(item);
            }
        }

        json.put("config", newConfig);
        return json.toString();
    }

    @Override
    public ExternalWorkPlan getExternalWorkPlan(WorkPlanIntegrationContext context, ValueSet values) {

        final String domain = values.get(AgmConstants.KEY_DOMAIN), projectId =
                values.get(AgmConstants.KEY_PROJECT), releaseId =
                values.get(AgmConstants.KEY_RELEASE), sprintId = values.get(AgmConstants.KEY_SPRINT);

        String dataDetailLevel = values.get(AgmConstants.KEY_DATA_DETAIL_LEVEL);

        if (StringUtils.isBlank(dataDetailLevel)) {
            dataDetailLevel = AgmConstants.DETAILS_ALL;
        }

        final Thread.UncaughtExceptionHandler exceptionHandler = new AGMConnectivityExceptionHandler();
        final Client client = new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL));

        EntityComplexTypeReader releaseEntity = null;

        // Check availability of AGM release
        try{
            clientUtils.setupClient(client, values);

            Entities releases = client.getReleaseById(values.get(AgmConstants.KEY_DOMAIN), values.get(AgmConstants.KEY_PROJECT), releaseId);

            if(releases.getTotalResults() == 0 || releases.getEntity().size() == 0){
                throw new AgmClientException("AGM_APP","ERROR_RELEASE_NOT_FOUND",releaseId);
            }

            releaseEntity = new EntityComplexTypeReader(releases.getEntity().get(0));

        }catch(Throwable e){
            logger.error(" fail:", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e, AGMIntegrationConnectorV2.class);
            return null;
        }

        final Map<String,List<EntityComplexTypeReader>> userStoriesDict = new HashMap<String,List<EntityComplexTypeReader>>();
        final Map<String,List<EntityComplexTypeReader>> usTaskDict = new HashMap<String,List<EntityComplexTypeReader>>();

        Thread taskWorker = new Thread(new Runnable(){

            @Override
            public void run() {
                //assigned-to
                //release-backlog-item-id
                //remaining estimated invested

                try{
                    final Entities usTasks = client.getTasks(domain, projectId, releaseId);

                    for(EntityComplexType e : usTasks.getEntity()){
                        EntityComplexTypeReader r = new EntityComplexTypeReader(e);
                        String backlogId = r.strValue("release-backlog-item-id");

                        if(!StringUtils.isEmpty(backlogId)){
                            getEntityContainer(usTaskDict, backlogId).add(r);
                        }
                    }

                    logger.info("TASK TOTAL="+usTasks.getTotalResults());
                }catch(ClientWebException e){
                    logger.error("", e);
                    logger.error("ERR="+e.getResponse().getStatusCode());
                }

                logger.info("TaskWorker done");
            }

        });
        taskWorker.setUncaughtExceptionHandler(exceptionHandler);
        taskWorker.start();

        Thread usWorker = new Thread(new Runnable(){

            @Override
            public void run() {

                final Entities userStories = client.getUserStoryByReleaseBlockItem(domain, projectId, releaseId);

                logger.info("US TOTAL=" + userStories.getTotalResults());

                for(EntityComplexType e : userStories.getEntity()){
                    EntityComplexTypeReader r = new EntityComplexTypeReader(e);
                    String sprintId = r.strValue("sprint-id");

                    if(!StringUtils.isEmpty(sprintId)){
                        getEntityContainer(userStoriesDict, sprintId).add(r);
                    }
                }

                logger.info("StoryWorker done");
            }
        });
        usWorker.setUncaughtExceptionHandler(exceptionHandler);
        usWorker.start();


        try{
            final Entities sprints = client.getSprintsByParentId(domain, projectId, releaseId);
            usWorker.join();
            taskWorker.join();

            if (!StringUtils.isBlank(sprintId)) {
                // Sprint-mapped task
                if (AgmConstants.DETAILS_SPRINT.equals(dataDetailLevel)) {
                    // Only show the sprint total effort
                    return wrapIExternalWorkPlan(getTotalSprintTask(sprintId, sprints,
                            userStoriesDict, usTaskDict));
                } else {
                    // Show sprint + US info
                    return wrapIExternalWorkPlan(getSingleSprintTask(sprintId, sprints,
                            userStoriesDict, usTaskDict));
                }
            } else {
                // Release-mapped task
                if (AgmConstants.DETAILS_RELEASE.equals(dataDetailLevel)) {
                    // Only show the release total effort
                    return wrapIExternalWorkPlan(getTotalReleaseTask(releaseEntity, sprints,
                            userStoriesDict, usTaskDict));
                }
                if (AgmConstants.DETAILS_SPRINT.equals(dataDetailLevel)) {
                    // Only show the sprint total effort for each sprint
                    List<ExternalTask> sprintTotalTasks =
                            new ArrayList<ExternalTask>(sprints.getEntity().size());
                    for (EntityComplexType s : sprints.getEntity()) {
                        final EntityComplexTypeReader spr = new EntityComplexTypeReader(s);
                        sprintTotalTasks.add(getTotalSprintTask(spr.strValue("id"), sprints,
                                userStoriesDict, usTaskDict));
                    }
                    return wrapIExternalWorkPlan(getTaskFromRelease(releaseEntity, sprintTotalTasks, null));
                } else {
                    // Show sprints + US info
                    List<ExternalTask> sprintTasks =
                            new ArrayList<ExternalTask>(sprints.getEntity().size());
                    for (EntityComplexType s : sprints.getEntity()) {
                        final EntityComplexTypeReader spr = new EntityComplexTypeReader(s);
                        sprintTasks.add(getSingleSprintTask(spr.strValue("id"), sprints,
                                userStoriesDict, usTaskDict));
                    }
                    return wrapIExternalWorkPlan(getTaskFromRelease(releaseEntity, sprintTasks, null));
                }
            }
        }catch(Throwable e){
            logger.error("", e);
            exceptionHandler.uncaughtException(Thread.currentThread(), e);
            return null;
        }
    }

    private ExternalTask getTaskFromRelease(final EntityComplexTypeReader release, final List<ExternalTask> sprintTasks,
            final List<ExternalTaskActuals> releaseActuals) {

        return new ExternalTask() {

            @Override
            public TaskStatus getStatus() {
                return TaskStatus.IN_PROGRESS;
            }

            @Override
            public Date getScheduledStart() {
                return release.dateValue("start-date", new Date(0));
            }

            @Override
            public Date getScheduledFinish() {
                return AGMWorkPlanIntegrationV2.this.adjustFinishDateTime(release.dateValue("end-date", new Date(0)));
            }

            @Override
            public String getName() {
                return release.strValue("name");
            }

            @Override
            public String getId() {
                return release.strValue("id");
            }

            @Override
            public List<ExternalTask> getChildren() {
                return sprintTasks;
            }

            @Override
            public List<ExternalTaskActuals> getActuals() {
                return releaseActuals;
            }

            @Override public Double getPercentCompleteOverrideValue() {
                if (releaseActuals != null && sprintTasks == null) {
                    // If release has some actuals & no children, it is a leaf task & we let PPM compute % complete from actuals
                    return null;
                } else {
                    // If release has no actuals or children, we'll override % complete to make sure it's effort based like in AgM
                    return computeSummaryTaskWeightedPercentComplete(this);
                }
            }
        };
  }

    private ExternalTask getTotalReleaseTask(final EntityComplexTypeReader release,
                                             Entities sprints,
                                             Map<String, List<EntityComplexTypeReader>> userStoriesDict,
                                             Map<String, List<EntityComplexTypeReader>> usTaskDict)
    {
        final List<ExternalTaskActuals> releaseActuals = new ArrayList<ExternalTaskActuals>();

        for (EntityComplexType s : sprints.getEntity()) {
            final EntityComplexTypeReader sprint = new EntityComplexTypeReader(s);

            List<ExternalTask> sprintUSTasks =
                    getSprintChildrenUSTasks(sprint, userStoriesDict, usTaskDict);

            for (ExternalTask us : sprintUSTasks) {
                releaseActuals.addAll(us.getActuals());
            }
        }

        return getTaskFromRelease(release, null, releaseActuals);
    }

    private ExternalTask getSingleSprintTask(final String sprintId, final Entities sprints,
                                             final Map<String, List<EntityComplexTypeReader>> userStoriesDict,
                                             final Map<String, List<EntityComplexTypeReader>> usTaskDict)
    {
        
        final EntityComplexTypeReader sprint = getSprint(sprintId, sprints);
        
        List<ExternalTask> usTasks = getSprintChildrenUSTasks(sprint, userStoriesDict, usTaskDict);
        
        // A sprint has no actual by itself, we get the actuals from the
        // children US.
        return getTaskFromSprint(sprint, null, usTasks);
    }
    
    private ExternalTask getTotalSprintTask(final String sprintId, final Entities sprints,
                                            final Map<String, List<EntityComplexTypeReader>> userStoriesDict,
                                            final Map<String, List<EntityComplexTypeReader>> usTaskDict)
    {
        final EntityComplexTypeReader sprint = getSprint(sprintId, sprints);

        List<ExternalTask> usTasks = getSprintChildrenUSTasks(sprint, userStoriesDict, usTaskDict);

        List<ExternalTaskActuals> sprintActuals = new ArrayList<ExternalTaskActuals>();

        for (ExternalTask us : usTasks) {
            sprintActuals.addAll(us.getActuals());
        }

        // A sprint total task has no child, and holds the US's actuals itself.
        return getTaskFromSprint(sprint, sprintActuals, null);
    }

    private List<ExternalTask> getSprintChildrenUSTasks(final EntityComplexTypeReader sprint,
                                                        final Map<String, List<EntityComplexTypeReader>> userStoriesDict,
                                                        final Map<String, List<EntityComplexTypeReader>> usTaskDict)
    {
        String sprintId = sprint.strValue("id");
        List<EntityComplexTypeReader> sprintUS = userStoriesDict.get(sprintId);

        if (sprintUS == null) {
            return new ArrayList<ExternalTask>();
        }

        List<ExternalTask> stories =
                new ArrayList<ExternalTask>(sprintUS == null ? 0 : sprintUS.size());

        for (final EntityComplexTypeReader us : sprintUS) {
            stories.add(new ExternalTask() {

                @Override
                public String getId() {
                    return us.strValue("id");
                }

                @Override
                public String getName() {
                    return us.strValue("entity-name");
                }

                @Override
                public TaskStatus getStatus() {
                    TaskStatus status = TaskStatus.READY;

                    switch (us.strValue("status")) {
                        case "Done":
                            status = TaskStatus.COMPLETED;
                            break;
                        case "In Progress":
                        case "In Testing":
                            status = TaskStatus.IN_PROGRESS;
                            break;
                        case "New":
                            status = TaskStatus.READY;
                            break;
                    }

                    return status;
                }

                @Override
                public Date getScheduledStart() {
                    // update for workplan sync - part 2:
                    // use US create date .
                    Date sprintStartDate =
                            AGMWorkPlanIntegrationV2.this.convertDate(sprint.strValue("start-date"));
                    Date sprintEndDate =
                            AGMWorkPlanIntegrationV2.this.convertDate(sprint.strValue("end-date"));
                    Date usCreationDate =
                            AGMWorkPlanIntegrationV2.this.convertDate(us.strValue("creation-date"));
                    if (sprintStartDate.before(usCreationDate)
                            && sprintEndDate.after(usCreationDate)) {
                        return usCreationDate;
                    }
                    return sprintStartDate;
                }

                @Override
                public Date getScheduledFinish() {
                    return AGMWorkPlanIntegrationV2.this.adjustFinishDateTime(AGMWorkPlanIntegrationV2.this
                            .convertDate(sprint.strValue("end-date")));
                }

                @Override
                public long getOwnerId() {
                    return -1;
                }

                @Override
                public String getOwnerRole() {
                    return null;
                }

                @Override
                public List<ExternalTask> getChildren() {
                    return null;
                }

                @Override
                public List<ExternalTaskActuals> getActuals() {
                    return AGMWorkPlanIntegrationV2.this.getUSActuals(usTaskDict, this.getId(),
                            this.getScheduledStart(), this.getScheduledFinish(), this.getStatus());
                }

                @Override
                public boolean isMilestone() {
                    return false;
                }
            });
        }
        return stories;
    }

    private List<ExternalTaskActuals> getUSActuals(
            final Map<String, List<EntityComplexTypeReader>> usTaskDict, final String usId,
            final Date userStoryScheduledStart, final Date userStoryScheduledFinish,
            final ExternalTask.TaskStatus userStoryStatus)
    {
        List<EntityComplexTypeReader> tasks = usTaskDict.get(usId);
        List<ExternalTaskActuals> actuals =
                new ArrayList<ExternalTaskActuals>(tasks == null ? 0 : tasks.size());

        final UserProvider userProvider = Providers.getUserProvider(AGMIntegrationConnector.class);

        if (tasks != null) {
            for (final EntityComplexTypeReader t : tasks) {
                actuals.add(new ExternalTaskActuals() {

                    @Override
                    public Date getActualStart() {
                        if (userStoryStatus != ExternalTask.TaskStatus.READY
                                || this.getPercentComplete() > 0) {
                            return userStoryScheduledStart;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public Date getActualFinish() {
                        if (this.getPercentComplete() >= 100) {
                            // means: status is Completed

                            //update for workplan sync
                            // - part 2:
                            // when task is Completed and last
                            // modify date after US schedule finish,
                            // use 'task' last modify date ' as
                            // actual finish.
                            Date taskLastModifyDate =
                                    AGMWorkPlanIntegrationV2.this.convertDate(t
                                            .strValue("status-date"));
                            if (userStoryScheduledFinish.after(taskLastModifyDate)
                                    && userStoryScheduledStart.before(
                                            taskLastModifyDate)) {
                                return AGMWorkPlanIntegrationV2.this.adjustFinishDateTime(taskLastModifyDate);
                            }
                            return AGMWorkPlanIntegrationV2.this.adjustFinishDateTime(userStoryScheduledFinish);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public double getActualEffort() {
                        return t.doubleValue("invested", 0);
                    }

                    public double getTotalEffort() {
                        return t.doubleValue("invested", 0)
                                + t.doubleValue("remaining", 0);
                    }

                    @Override
                    public double getPercentComplete() {
                        return (this.getTotalEffort() == 0 ? 0 : this
                                .getActualEffort() / this.getTotalEffort() * 100);
                    }

                    @Override
                    public long getResourceId() {
                        User u = userProvider.getByEmail(t.strValue("assigned-to"));
                        return u == null ? -1 : u.getUserId();
                    }

                    @Override
                    public double getScheduledEffort() {
                        return t.doubleValue("estimated", 0);
                    }

                    @Override
                    public Double getEstimatedRemainingEffort() {
                        return t.doubleObjectValue("remaining");
                    }
                });
            }
        }

        return actuals;
    }

    private ExternalTask getTaskFromSprint(final EntityComplexTypeReader sprint,
                                           final List<ExternalTaskActuals> sprintActuals, final List<ExternalTask> children)
    {
        return new ExternalTask() {

            @Override
            public String getId() {
                return sprint.strValue("id");
            }

            @Override
            public String getName() {
                return sprint.strValue("name");
            }

            @Override
            public TaskStatus getStatus() {
                if (children == null || children.isEmpty()) {
                    // This is a standalone sprint, we need to have actuals
                    // properly displayed.
                    // Setting status to READY would not save any actuals.
                    if (getActuals() == null || getActuals().isEmpty()) {
                        return TaskStatus.READY;
                    }
                    boolean isComplete = true;
                    boolean isInProgress = false;
                    for (ExternalTaskActuals actual : getActuals()) {
                        if (actual.getPercentComplete() < 99.995) {
                            isComplete = false;
                        }
                        if (actual.getPercentComplete() > 0.005) {
                            isInProgress = true;
                        }
                    }

                    if (isComplete) {
                        return TaskStatus.COMPLETED;
                    } else if (isInProgress) {
                        return TaskStatus.IN_PROGRESS;
                    } else {
                        return TaskStatus.READY;
                    }
                } else {
                    // Status will be rolled up from US children tasks.
                    return TaskStatus.READY;
                }

            }

            @Override
            public Date getScheduledStart() {
                return AGMWorkPlanIntegrationV2.this.convertDate(sprint.strValue("start-date"));
            }

            @Override
            public Date getScheduledFinish() {
                return AGMWorkPlanIntegrationV2.this.adjustFinishDateTime(AGMWorkPlanIntegrationV2.this
                        .convertDate(sprint.strValue("end-date")));
            }

            @Override
            public long getOwnerId() {
                return -1;
            }

            @Override
            public List<ExternalTaskActuals> getActuals() {
                return sprintActuals;
            }

            @Override
            public String getOwnerRole() {
                return "";
            }

            @Override
            public List<ExternalTask> getChildren() {
                return children;
            }

            @Override
            public Double getPercentCompleteOverrideValue() {
                return computeSummaryTaskWeightedPercentComplete(this);
            }

            @Override
            public boolean isMilestone() {
                return false;
            }
        };
    }

 

    private EntityComplexTypeReader getSprint(String sprintId, Entities sprints) {
        for(EntityComplexType s : sprints.getEntity()){
            final EntityComplexTypeReader sprint = new EntityComplexTypeReader(s);
            
            if (sprintId.equalsIgnoreCase(sprint.strValue("id"))) {
                return sprint;
            }
        }
        
        throw new AgmClientException("AGM_APP", "ERROR_SPRINT_NOT_FOUND", sprintId);
    }

    private ExternalWorkPlan wrapIExternalWorkPlan(final ExternalTask... rootTasks) {
        return new ExternalWorkPlan() {

            @Override
            public List<ExternalTask> getRootTasks() {
                return Arrays.asList(rootTasks);
            }
        };
    }

    private ExternalWorkPlan wrapIExternalWorkPlan(final List<ExternalTask> rootTasks) {
        return new ExternalWorkPlan() {
            @Override
            public List<ExternalTask> getRootTasks() {
                return rootTasks;
            }
        };
    }

    protected Date convertDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            logger.error("Exception when parsing date string '" + dateStr + "', returning new date()", e);
        }
        return new Date();
    }

    /**
     * Dates returned from AgM don't have time set and are set to midnight (i.e.
     * start of the day). However, a date set as a finish date usually means
     * "after the day as finished", and we need to set the hour to the end of
     * the day so that PPM computes duration correctly. <br>
     * This will modify the date passed in parameter if needed and then return
     * it.
     */
    @SuppressWarnings("deprecation")
    protected Date adjustFinishDateTime(Date date) {
        if (date == null) {
            return null;
        }

        if (date.getHours() < 23) {
            date.setHours(23);
        }

        return date;
    }

    private List<EntityComplexTypeReader> getEntityContainer(Map<String,List<EntityComplexTypeReader>> map, String key){
        List<EntityComplexTypeReader> container;
        if(!map.containsKey(key)){
            map.put(key, new LinkedList<EntityComplexTypeReader>());
        }
        container = map.get(key);

        return container;
    }

    /**
     * To be used only on summary tasks, this method will compute the % complete of the summary task in the same way as AgM, i.e. with weighted effort.
     * We want to use this instead of letting PPM do the % rollup based on tasks durations.
     */
    protected Double computeSummaryTaskWeightedPercentComplete(ExternalTask summaryTask) {

        if (summaryTask.getChildren() == null || summaryTask.getChildren().isEmpty()) {
            // This is not really a summary task, as it has no children and is thus a leaf task. We return null,
            // as we expect % complete to be zero, but may let PPM compute it from work units if by any chance there's any.
            return null;
        }

        // Summary tasks don't have actuals, only their children do.
        double totalpercent = 0d;
        double totalWeight  = 0d;

        List<ExternalTask> remainingTasks = new ArrayList<ExternalTask>();

        remainingTasks.add(summaryTask);

        while (remainingTasks != null && !remainingTasks.isEmpty()) {
            List<ExternalTask> remainingSummaryTasks = new ArrayList<ExternalTask>();

            for (ExternalTask task : remainingTasks) {

                if (task.getChildren() == null) {
                    System.out.println("No way!");
                }

                for (ExternalTask child: task.getChildren()) {
                    if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                        // Summary task, let's queue it.
                        remainingSummaryTasks.add(child);
                    } else {
                        // Leaf task, let's add its actuals to percent complete. It's weighted by AE + ERE
                        // There's no reason for an AgM leaf task to already have a percent complete override value, we only use this for summary tasks.
                        if (child.getActuals() == null) {
                            // This can happen
                            continue;
                        }
                        for (ExternalTaskActuals actuals : child.getActuals()) {
                            double weight = 0d;
                            if (actuals.getEstimatedRemainingEffort() != null) {
                                weight = actuals.getActualEffort() + actuals.getEstimatedRemainingEffort();
                            } else {
                                weight = actuals.getScheduledEffort();
                            }

                            double weightedPercentComplete = actuals.getPercentComplete() * weight;

                            totalpercent += weightedPercentComplete;
                            totalWeight += weight;
                        }
                    }
                }
            }

            remainingTasks = remainingSummaryTasks;
        } ;

        if (totalWeight <= 0) {
            return 0d;
        } else {
            return totalpercent / totalWeight;
        }
    }

    @Override
    public String getCustomDetailPage() {
        return "/itg/integrationcenter/agm-connector-impl-web/agm-graphs.jsp";
    }
}
