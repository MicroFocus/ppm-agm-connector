package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.pm.*;
import com.ppm.integration.agilesdk.provider.Providers;
import com.ppm.integration.agilesdk.provider.UserProvider;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.Projects;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.ppm.integration.agilesdk.ui.*;
import com.hp.ppm.user.model.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientWebException;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AGMWorkPlanIntegration extends WorkPlanIntegration {

    protected AGMClientUtils clientUtils = new AGMClientUtils();

    private final Logger logger = Logger.getLogger(this.getClass());

    protected Client getClient(ValueSet values){
        Client client = new Client(values.get(AgmConstants.KEY_BASE_URL));
        clientUtils.setupClient(client, values);
        return client;
    }

    @Override
    public List<Field> getMappingConfigurationFields(WorkPlanIntegrationContext context,ValueSet values) {

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

                    Projects projects =client.getProjects(values.get(AgmConstants.KEY_DOMAIN));
                    List<Option> options = new ArrayList<Option>(projects.getCollection().size());
                    for(Project p : projects.getCollection()){
                        options.add(new Option(p.name,p.name));
                    }
                    return options;
                }
            },
            new AgmEntityDropdown(AgmConstants.KEY_RELEASE,"RELEASE","block",true){

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

                    Entities releases = client.getReleases(values.get(AgmConstants.KEY_DOMAIN), values.get(AgmConstants.KEY_PROJECT));

                    List<Option> options = new ArrayList<Option>(releases.getEntity().size());

                    for(EntityComplexType e : releases.getEntity()){
                        EntityComplexTypeReader reader = new EntityComplexTypeReader(e);

                        options.add(new Option(reader.strValue("id"),reader.strValue("name")));
                    }
                    return options;
                }
            }

            ,new LineBreaker()
            ,new CheckBox(AgmConstants.KEY_SHOW_SPRINT_BURN_DOWN,"SHOW_SPRINT_BURN_DOWN","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_RELEASE_BURN_UP,"SHOW_RELEASE_BURN_UP","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_THEME_STATUS,"SHOW_THEME_STATUS","block",true)
            ,new CheckBox(AgmConstants.KEY_SHOW_FEATURE_STATUS,"SHOW_FEATURE_STATUS","block",true)
        });
    }

    @Override
    public ExternalWorkPlan getExternalWorkPlan(WorkPlanIntegrationContext context, ValueSet values) {

        final String domain = values.get(AgmConstants.KEY_DOMAIN),
                     project = values.get(AgmConstants.KEY_PROJECT),
                     release = values.get(AgmConstants.KEY_RELEASE);

        final UncaughtExceptionHandler exceptionHandler = new AGMConnectivityExceptionHandler();
        final Client client = new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL));

        // Check availability of AGM release
        try{
            clientUtils.setupClient(client, values);

            Entities releases = client.getReleaseById(values.get(AgmConstants.KEY_DOMAIN), values.get(AgmConstants.KEY_PROJECT), release);

            if(releases.getTotalResults() == 0 || releases.getEntity().size() == 0){
                throw new AgmClientException("AGM_APP","ERROR_RELEASE_NOT_FOUND",release);
            }

            EntityComplexTypeReader firstEntity = new EntityComplexTypeReader(releases.getEntity().get(0));

        }catch(Throwable e){
            logger.error("setupClient error:", e);
            exceptionHandler.uncaughtException(Thread.currentThread(), e);
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
                    final Entities usTasks =client.getTasks(domain, project, release);

                    for(EntityComplexType e : usTasks.getEntity()){
                        EntityComplexTypeReader r = new EntityComplexTypeReader(e);
                        String backlogId = r.strValue("release-backlog-item-id");

                        if(!StringUtils.isEmpty(backlogId)){
                            getEntityContainer(usTaskDict, backlogId).add(r);
                        }
                    }

                    logger.info("TASK TOTAL="+usTasks.getTotalResults());
                }catch(ClientWebException e){
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

                final Entities userStories =client.getUserStoryByReleaseBlockItem(domain, project, release);

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
            final Entities sprints = client.getSprintsByParentId(domain, project, release);
            usWorker.join();
            taskWorker.join();

            final UserProvider userProvider = Providers.getUserProvider(AGMIntegrationConnector.class);
            return new ExternalWorkPlan(){

                @Override
                public List<ExternalTask> getRootTasks() {
                    List<ExternalTask> rootTasks = new LinkedList<ExternalTask>();

                    //int lastSprint = sprints.getEntity().size() - 2, thisSprint = 0;
                    for(EntityComplexType s : sprints.getEntity()){
                        final EntityComplexTypeReader sprint = new EntityComplexTypeReader(s);

                        /*
                        if(thisSprint++ < lastSprint){
                            continue;
                        }
                        */

                        rootTasks.add(new ExternalTask(){

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

                                return TaskStatus.READY;
                            }

                            @Override
                            public Date getScheduledStart() {
                                return AGMWorkPlanIntegration.this.convertDate(sprint.strValue("start-date"));
                            }

                            @Override
                            public Date getScheduledFinish() {
                                return AGMWorkPlanIntegration.this.convertDate(sprint.strValue("end-date"));
                            }

                            @Override
                            public long getOwnerId() {
                                return -1;
                            }

                            @Override
                            public List<ExternalTaskActuals> getActuals() {
                                return null;
                            }

                            @Override
                            public String getOwnerRole() {
                                return "";
                            }

                            @Override
                            public List<ExternalTask> getChildren() {
                                String sprintId = this.getId();
                                List<EntityComplexTypeReader> sprintUS = userStoriesDict.get(sprintId);

                                if(sprintUS==null){
                                    return null;
                                }

                                List<ExternalTask> list = new ArrayList<ExternalTask>(sprintUS == null?0:sprintUS.size());
                                for(final EntityComplexTypeReader us : sprintUS ){
                                    list.add(new ExternalTask(){

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

                                            switch(us.strValue("status")){
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
                                            return AGMWorkPlanIntegration.this.convertDate(sprint.strValue("start-date"));
                                        }

                                        @Override
                                        public Date getScheduledFinish() {
                                            return AGMWorkPlanIntegration.this.convertDate(sprint.strValue("end-date"));
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
                                            final ExternalTask story = this;
                                            List<EntityComplexTypeReader> tasks = usTaskDict.get(this.getId());
                                            List<ExternalTaskActuals> actuals = new ArrayList<ExternalTaskActuals>(tasks==null?0:tasks.size());

                                            if(tasks!=null){
                                                for(final EntityComplexTypeReader t : tasks){
                                                    actuals.add(new ExternalTaskActuals(){

                                                        @Override
                                                        public Date getActualStart() {
                                                            if(story.getStatus()!=TaskStatus.READY || this.getPercentComplete() > 0){
                                                                return story.getScheduledStart();
                                                            }else{
                                                                return null;
                                                            }
                                                        }

                                                        @Override
                                                        public Date getActualFinish() {
                                                            if(this.getPercentComplete() >= 100){
                                                                return story.getScheduledFinish();
                                                            }else{
                                                                return null;
                                                            }
                                                        }

                                                        @Override
                                                        public double getActualEffort() {
                                                            return t.doubleValue("invested", 0);
                                                        }

                                                        public double getTotalEffort() {
                                                            return t.doubleValue("invested", 0) + t.doubleValue("remaining", 0);
                                                        }

                                                        @Override
                                                        public double getPercentComplete() {
                                                            return (this.getTotalEffort() == 0 ? 0 : this.getActualEffort() / this.getTotalEffort() * 100 );
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
                                                    });
                                                }
                                            }

                                            return actuals;
                                        }

                                        @Override
                                        public boolean isMilestone() {
                                            return false;
                                        }
                                    });
                                }
                                return list;
                            }

                            @Override
                            public boolean isMilestone() {
                                return false;
                            }

                        });
                    }
                    return rootTasks;
                }
            };

        }catch(Throwable e){
            logger.error("error:", e);
            exceptionHandler.uncaughtException(Thread.currentThread(), e);
            return null;
        }
    }

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //SimpleDateFormat is not thread safe.
    protected synchronized Date convertDate(String dateStr){
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return new Date();

    }

    private List<EntityComplexTypeReader> getEntityContainer(Map<String,List<EntityComplexTypeReader>> map, String key){
        List<EntityComplexTypeReader> container;
        if(!map.containsKey(key)){
            map.put(key, new LinkedList<EntityComplexTypeReader>());
        }
        container = map.get(key);

        return container;
    }

    @Override
    public String getCustomDetailPage() {
        return "/itg/integrationcenter/agm-connector-impl-web/agm-graphs.jsp";
    }
}
