package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.client.EntityComplexTypeTransformer;
import com.ppm.integration.agilesdk.connector.agm.client.EntityComplexTypeTransformer.Filter;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.tm.*;
import com.ppm.integration.agilesdk.ui.*;
import com.hp.ppm.tm.model.TimeSheet;
import com.kintana.tmg.util.TMGConstants;
import com.mercury.itg.core.calendar.model.ITGSchedulingCalendar;
import com.mercury.itg.tm.util.TMUtil;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientRuntimeException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class AGMTimeSheetIntegration extends TimeSheetIntegration {

    protected AGMClientUtils clientUtils = new AGMClientUtils();

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public List<Field> getMappingConfigurationFields(ValueSet values) {
        return Arrays.asList(new Field[]{
                new PlainText(AgmConstants.KEY_USERNAME,"USERNAME","","block",true),
                new PasswordText(AgmConstants.KEY_PASSWORD,"PASSWORD","","block",true)
        });
    }

    private static ExecutorService domainFetcherService = Executors.newFixedThreadPool(2);
    private static ExecutorService projectFetcherService = Executors.newFixedThreadPool(5);
    private static ExecutorService releaseFetcherService = Executors.newFixedThreadPool(5);

    @Override
    public List<ExternalWorkItem> getExternalWorkItems(TimeSheetIntegrationContext context, final ValueSet values) {

        final List<ExternalWorkItem> items = getExternalWorkItemsByTasks(context, values);

        return items;
    }

    public List<ExternalWorkItem> getExternalWorkItemsByTasks(TimeSheetIntegrationContext context, final ValueSet values) {
        final List<ExternalWorkItem> items = Collections.synchronizedList(new LinkedList<ExternalWorkItem>());
        try {
            final Client simpleClient = clientUtils.setupClient(new Client(values.get(AgmConstants.KEY_BASE_URL)),values);
            final Client entitiesClient = clientUtils.setupClient(new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)),simpleClient.getCookies(),values);


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
                        for(final Project p : simpleClient.getProjects(d.name).getCollection()){

                            waitProjectQueue.add(projectFetcherService.submit(new Callable<Boolean>(){

                                @Override
                                public Boolean call() throws Exception {

                                    Entities releases =entitiesClient.getCurrentReleases(d.name, p.name);
                                    Future<List<EntityComplexType>> jobSprints = releaseFetcherService.submit(new Callable<List<EntityComplexType>>(){

                                        @Override
                                        public List<EntityComplexType> call() throws Exception {

                                            return entitiesClient.getSprints(d.name,p.name);
                                        }
                                    });

                                    Future<List<EntityComplexType>> jobTasks = releaseFetcherService.submit(new Callable<List<EntityComplexType>>(){

                                        @Override
                                        public List<EntityComplexType> call() throws Exception {

                                            return entitiesClient.getCompletedTasks(d.name, p.name, values.get(AgmConstants.KEY_USERNAME));
                                        }
                                    });

                                    List<EntityComplexType> sprints = jobSprints.get();
                                    List<EntityComplexType> tasks = jobTasks.get();

                                    for( EntityComplexType r : releases.getEntity() ){
                                        final EntityComplexTypeReader release = new EntityComplexTypeReader(r);
                                        items.add(new AGMExternalWorkItem(d.name,p.name, release,
                                            EntityComplexTypeTransformer.wrap(sprints, new Filter() {

                                                        @Override
                                                        public boolean isMatched(EntityComplexTypeReader sprint) {
                                                            return release.strValue("id").equals(sprint.strValue("parent-id"));
                                                        }
                                                    }
                                            ),
                                            EntityComplexTypeTransformer.wrap(tasks, new Filter(){

                                                @Override
                                                public boolean isMatched( EntityComplexTypeReader task) {
                                                    return release.strValue("id").equals( task.getRelatedEntityReaderByAlias("release-backlog-item").strValue("release-id") );
                                                }}
                                            ),
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
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e.getCause());
        } catch (ClientRuntimeException e){
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        } catch (AgmClientException e){
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        } catch (RuntimeException e){
            logger.error("", e);
            new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }

        return items;
    }


    private class AGMExternalWorkItem extends ExternalWorkItem {

        final String domain;
        final String project;
        final EntityComplexTypeReader release;
        static final String SEP = "&#&";

        double totalEffort = 0d;
        String errorMessage = null;
        private ValueSet config;
        Date startDate;
        Date finishDate;

        public AGMExternalWorkItem(String domain, String project,
                EntityComplexTypeReader release,
                List<EntityComplexTypeReader> sprints, List<EntityComplexTypeReader> tasks,
                ValueSet values,
                Date startDate, Date finishDate) {

            this.domain = domain;
            this.project = project;
            this.release = release;
            this.config = values;
            this.startDate = startDate;
            this.finishDate = finishDate;

            for(EntityComplexTypeReader sprint : sprints){

                Date sprintStartDate = sprint.dateValue("start-date", new Date());
                Date sprintFinishDate = sprint.dateValue("end-date", new Date());

                if( !sprintStartDate.before(startDate) && !sprintFinishDate.after(finishDate) ){
                    //sprints inner startDate and finishDate
                    for(EntityComplexTypeReader task : tasks){

                        if( sprint.strValue("id").equals( task.getRelatedEntityReaderByAlias("release-backlog-item").strValue("sprint-id") ) )
                        totalEffort += task.doubleValue("invested", 0f);
                    }
                }else if( (sprintStartDate.before(startDate) && !sprintFinishDate.before(startDate)) || (!sprintStartDate.after(finishDate) && sprintFinishDate.after(finishDate)) ){
                    errorMessage = "WARN_TM_AGM_RELEASE_NOT_ALIGNED";
                }
            }
        }

        @Override
        public String getName() {
            return this.domain + SEP + this.project + SEP + this.release.strValue("name");
        }

        @Override
        public Double getTotalEffort() {
            return totalEffort;
        }

        @Override
        public ExternalWorkItemEffortBreakdown getEffortBreakDown() {

            ExternalWorkItemEffortBreakdown effortBreakdown = new ExternalWorkItemEffortBreakdown();

            final ITGSchedulingCalendar ppmCalendar = TMUtil.getDefaultCalendar();
            final long numOfWorkDays =
                    ppmCalendar.getNumOfWorkDays(startDate, finishDate);

            if (numOfWorkDays > 0) {
                double amountPerDay = 0d;
                if (this.getTotalEffort() != 0) {
                    amountPerDay = round2decimals(this.getTotalEffort() / numOfWorkDays);
                }
                double firstDayAmount = round2decimals(this.getTotalEffort() - amountPerDay * (numOfWorkDays -  1));
                boolean beFirstDay = true;
                ExternalWorkItemEffortBreakdown actual = new ExternalWorkItemEffortBreakdown();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startDate);
                int diffDays = getDaysDiffNumber(startDate, finishDate);
                for(int i = 0; i < diffDays; i++) {
                    if (ppmCalendar.isWorkDay(calendar.getTime())) {
                        if (beFirstDay) {
                            beFirstDay = false;
                            effortBreakdown.addEffort(calendar.getTime(), firstDayAmount);
                        } else {
                            effortBreakdown.addEffort(calendar.getTime(), amountPerDay);
                        }
                    } else {
                        effortBreakdown.addEffort(calendar.getTime(), 0d);
                    }

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
    }

    private int getDaysDiffNumber(Date startDate, Date endDate) {
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

    private double round2decimals(final double d) {
        final BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.divide(new BigDecimal(1), TMGConstants.NO_OF_DECIMAL_DIGITS_HOURS_POLICY,
                BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
