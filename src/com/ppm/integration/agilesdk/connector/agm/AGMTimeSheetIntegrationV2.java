package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.client.EntityComplexTypeTransformer;
import com.ppm.integration.agilesdk.connector.agm.client.EntityComplexTypeTransformer.Filter;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.AccessToken;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.TimesheetItem;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.tm.*;
import com.ppm.integration.agilesdk.ui.*;
import com.hp.ppm.tm.model.TimeSheet;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientRuntimeException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class AGMTimeSheetIntegrationV2 extends AGMTimeSheetIntegration {

    private final Logger logger = Logger.getLogger(this.getClass());

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //thread safe
    protected synchronized String convertDate(Date date){

        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";

    }

    @Override
    public List<Field> getMappingConfigurationFields(ValueSet values) {
        return Arrays.asList(new Field[]{
                new PlainText(AgmConstants.KEY_USERNAME,"USERNAME","",true),
                new PasswordText(AgmConstants.KEY_PASSWORD,"PASSWORD","",true)
        });
    }

    private static ExecutorService domainFetcherService = Executors.newFixedThreadPool(1);
    private static ExecutorService projectFetcherService = Executors.newFixedThreadPool(2);
    private static ExecutorService releaseFetcherService = Executors.newFixedThreadPool(2);


    @Override
    public List<ExternalWorkItem> getExternalWorkItems(TimeSheetIntegrationContext context, final ValueSet values) {

        String clientId = values.get(AgmConstants.APP_CLIENT_ID);
        String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
        checkClientIdSet(clientId, clientSecret);
        final List<ExternalWorkItem> items = getExternalWorkItemsByTimesheets(context, values);
        return items;
    }

    public List<ExternalWorkItem> getExternalWorkItemsByTimesheets(TimeSheetIntegrationContext context, final ValueSet values) {

        final List<ExternalWorkItem> items = Collections.synchronizedList(new LinkedList<ExternalWorkItem>());
        try {

            final Client simpleClient = clientUtils.setupClient(new Client(values.get(AgmConstants.KEY_BASE_URL)), values);
            final Client entitiesClient = clientUtils.setupClient(new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)), simpleClient.getCookies(), values);
            //the auth is in this method: setupClient(new Client(values.get(AgmConstants.KEY_BASE_URL)),values);
            final boolean passAuth = true;
            List<Domain> domains = entitiesClient.getDomains();

            final List<Future<Boolean>> waitDomainQueue = new ArrayList<Future<Boolean>>(domains.size());
            final List<Future<Boolean>> waitProjectQueue = Collections.synchronizedList(new ArrayList<Future<Boolean>>(domains.size()));

            TimeSheet timeSheet = context.currentTimeSheet();

            final Date startDate = timeSheet.getPeriodStartDate().toGregorianCalendar().getTime();
            final Date endDate = timeSheet.getPeriodEndDate().toGregorianCalendar().getTime();

            for(final Domain d : domains){

                waitDomainQueue.add(domainFetcherService.submit(new Callable<Boolean>(){

                    @Override
                    public Boolean call() throws Exception {
                        List<Project> projects = new ArrayList<Project>();
                        try {
                            projects = simpleClient.getProjects(d.name).getCollection();
                        }catch(Exception e) {
                            logger.error(" error of getProjects in domain [" + d.name + "]", e);
                        }
                        for(final Project p : projects){

                            waitProjectQueue.add(projectFetcherService.submit(new Callable<Boolean>(){

                                @Override
                                public Boolean call() throws Exception {

                                    Entities releases =entitiesClient.getCurrentReleases(d.name, p.name);
                                    Future<List<EntityComplexType>> jobSprints = releaseFetcherService.submit(new Callable<List<EntityComplexType>>() {

                                        @Override
                                        public List<EntityComplexType> call() throws Exception {

                                            return entitiesClient.getSprints(d.name, p.name);
                                        }
                                    });


                                    ClientPublicAPI client = clientUtils.setupClientPublicAPI(values);
                                    String clientId = values.get(AgmConstants.APP_CLIENT_ID);
                                    String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
                                    checkClientIdSet(clientId, clientSecret);
                                    AccessToken token = client.getAccessTokenWithFormFormat(clientId, clientSecret);

                                    if(!passAuth) {
                                        throw new AgmClientException("AGM_API","ERROR_AUTHENTICATION_FAILED");
                                    }

                                    List<TimesheetItem> timeSheets = new ArrayList<TimesheetItem>();
                                    Entities workspaces = simpleClient.getWorkSpaces(d.name, p.name);
                                    for(EntityComplexType e : workspaces.getEntity()){
                                        EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
                                        int workspaceId = reader.intValue("id", 0);
                                        if (workspaceId > 0) {
                                            try {
                                                List<TimesheetItem> thisTimeSheets = client.getTimeSheetData(token,
                                                        values.get(AgmConstants.KEY_USERNAME),
                                                        convertDate(startDate), convertDate(endDate),
                                                        workspaceId);

                                                timeSheets.addAll(thisTimeSheets);
                                            } catch (Exception exception) {
                                                logger.error("error in getTimeSheetData:", exception);
                                            }
                                        }
                                    }

                                    List<EntityComplexType> sprints = jobSprints.get();


                                    for( EntityComplexType r : releases.getEntity() ){
                                        final EntityComplexTypeReader release = new EntityComplexTypeReader(r);
                                        items.add(new AGMExternalWorkItem(d.name,p.name, release,
                                                EntityComplexTypeTransformer.wrap(sprints, new Filter(){

                                                            @Override
                                                            public boolean isMatched( EntityComplexTypeReader sprint) {
                                                                return release.strValue("id").equals(sprint.strValue("parent-id"));
                                                            }}
                                                ),
                                                timeSheets,
                                                values,
                                                startDate, endDate));
                                    }
                                    return true;
                                }
                            }));
                        }

                        return true;
                    }
                }));
            }


            for(List<Future<Boolean>> waitQueue : new List[]{waitDomainQueue,waitProjectQueue}){
                for(Future<Boolean> f : waitQueue){
                    f.get();
                }
            }

            Collections.sort(items, new Comparator<ExternalWorkItem>(){

                @Override
                public int compare(ExternalWorkItem o1, ExternalWorkItem o2) {
                    AGMExternalWorkItem a1 = (AGMExternalWorkItem) o1;
                    AGMExternalWorkItem a2 = (AGMExternalWorkItem) o2;

                    int result = 0, factor = 100000;
                    for(int f : new int[]{
                            a1.domain.compareTo(a2.domain),
                            a1.project.compareTo(a2.project),
                            a1.release.strValue("name").compareTo(a2.release.strValue("name"))}) {

                        if(f < 0){
                            result += -1 * factor;
                        }else if(f > 0){
                            result += +1 * factor;
                        }
                        factor /= 10;
                    }

                    return result;
                }
            });
        } catch (InterruptedException e) {
            logger.error("", e);

        } catch (ExecutionException e) {
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e.getCause(), AGMIntegrationConnectorV2.class);
        } catch (ClientRuntimeException | AgmClientException e){
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e, AGMIntegrationConnectorV2.class);
        } catch (RuntimeException e){
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e, AGMIntegrationConnectorV2.class);
        }

        return items;
    }


    public void checkClientIdSet(String clientId , String clientSecret) {
        if( null == clientId || clientId.trim().length() == 0
                || null == clientSecret || clientSecret.trim().length() == 0 ) {
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), new AgmClientException("AGM_APP", "CLIENT_ID_OR_SECRET_KEY_NOT_SET"), AGMIntegrationConnectorV2.class);
        }
    }


    private class AGMExternalWorkItem extends ExternalWorkItem {

        final String domain;
        final String project;
        final EntityComplexTypeReader release;
        static final String SEP = ">";

        double totalEffort = 0d;
        String errorMessage = null;
        private ValueSet config;
        Date startDate;
        Date finishDate;
        Hashtable<String, Long> effortList = new Hashtable<>();


        public AGMExternalWorkItem(String domain, String project,
                                   EntityComplexTypeReader release,
                                   List<EntityComplexTypeReader> sprints, List<TimesheetItem> items,
                                   ValueSet values,
                                   Date startDate, Date finishDate) {

            this.domain = domain;
            this.project = project;
            this.release = release;
            this.config = values;
            this.startDate = startDate;
            this.finishDate = finishDate;

            for(EntityComplexTypeReader sprint : sprints){
                for(TimesheetItem item : items){
                    if( sprint.strValue("id").equals(String.valueOf(item.getSprintId())) ) {
                        totalEffort += item.getInvested();
                        Long effort = effortList.get(item.getDate());
                        if(effort == null) effort = 0L;
                        effortList.put(item.getDate(), item.getInvested() + effort);
                    }
                }
            }
        }

        @Override
        public String getName() {
            return this.release.strValue("name") + "(" + this.domain + SEP + this.project + ")";
        }

        @Override
        public Double getTotalEffort() {
            return totalEffort;
        }

        @Override
        public ExternalWorkItemEffortBreakdown getEffortBreakDown() {

            ExternalWorkItemEffortBreakdown effortBreakdown = new ExternalWorkItemEffortBreakdown();

            int numOfWorkDays = getDaysDiffNumber(startDate, finishDate);

            if (numOfWorkDays > 0) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startDate);

                for(int i = 0; i < numOfWorkDays; i++) {
                    Long effort = effortList.get(convertDate(calendar.getTime()));
                    if(effort == null) effort = 0L;
                    effortBreakdown.addEffort(calendar.getTime(), effort.doubleValue());
                    // move to next day
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            return effortBreakdown;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        public int getDaysDiffNumber(Date startDate, Date endDate) {
            Calendar start = new GregorianCalendar();
            start.setTime(startDate);

            Calendar end = new GregorianCalendar();
            end.setTime(endDate);
            //move to last millsecond
            end.set(Calendar.HOUR_OF_DAY,23);
            end.set(Calendar.MINUTE,59);
            end.set(Calendar.SECOND,59);
            end.set(Calendar.MILLISECOND,999);

            Calendar dayDiff =  Calendar.getInstance();
            dayDiff.setTime(startDate);
            int diffNumber  = 0;
            while (dayDiff.before(end)) {
                diffNumber ++;
                dayDiff.add(Calendar.DAY_OF_MONTH, 1);
            }
            return diffNumber;
        }
    }

}
