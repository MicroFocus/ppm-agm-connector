package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.tm.model.TimeSheet;
import com.mercury.itg.core.calendar.model.ITGSchedulingCalendar;
import com.mercury.itg.tm.util.TMUtil;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.tm.ExternalWorkItem;
import com.ppm.integration.agilesdk.ui.Field;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;

/**
 * Created by libingc on 2/25/2016.
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"TMUtil"})
@PrepareForTest( {AGMTimeSheetIntegration.class,TMUtil.class})
@PowerMockIgnore("javax.management.*")
public class AGMTimeSheetIntegrationTestMock {


    Client entitiesClient = null;
    Client client = null;
    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();

        client=new MockClient("https://lottie.saas.hp.com");
        PowerMockito.whenNew(Client.class).withAnyArguments().thenReturn(client);
        entitiesClient=new MockClient("https://lottie.saas.hp.com");
        PowerMockito.whenNew(EntitiesClient.class).withAnyArguments().thenReturn((EntitiesClient) entitiesClient);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetMappingConfigurationFields(){

        AGMTimeSheetIntegration asi = new AGMTimeSheetIntegration();
        List<Field> fields = asi.getMappingConfigurationFields(values);
        Assert.assertNotNull(fields);
        int expectedSize = 2;
        Assert.assertEquals(expectedSize, fields.size());
    }

    @Test
    public void testGetExternalWorkItems(){
        TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
        AGMTimeSheetIntegration asi = new AGMTimeSheetIntegration();
        List<ExternalWorkItem> fields = asi.getExternalWorkItems(context,values);
        Assert.assertNotNull(fields);
    }
/*	
    @Test
    public void testGetDaysDiffNumber(){
        Date startDate=new Date(2013,12,1);
        Date endDate=new Date(2016, 1, 12);
        int temp=AGMTimeSheetIntegration.getDaysDiffNumber(startDate, endDate);
        Assert.assertNotNull(temp);
    }

    @Test
    public void testRound2decimals(){
        double temp=AGMTimeSheetIntegration.round2decimals(1.0);
        Assert.assertNotNull(temp);
    }
*/
    @Test
    public void testGetExternalWorkItemsByTasks() throws Exception{
        TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
        AGMTimeSheetIntegration asi = new AGMTimeSheetIntegration();

        ITGSchedulingCalendar ppmCalendar=PowerMockito.mock(ITGSchedulingCalendar.class);
        PowerMockito.mockStatic(TMUtil.class);
        PowerMockito.when(TMUtil.getDefaultCalendar()).thenReturn(ppmCalendar);
        PowerMockito.when(ppmCalendar.getNumOfWorkDays(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn((long) 12);
        PowerMockito.when(ppmCalendar.isWorkDay(Mockito.any(Date.class))).thenReturn(true);
        List<ExternalWorkItem> items = asi.getExternalWorkItemsByTasks(context, values);

        Assert.assertNotNull(items);
        for (ExternalWorkItem item : items) {
            System.out.println("name=" + item.getName() + ", effort="
                            + item.getTotalEffort() + ", errorMessage=" + item.getErrorMessage()
                    + ", ExternalData=" + item.getEffortBreakDown().toJsonString());
        }
    }


    @Test
    public void testGetTimeSheet() {
        try {
            AGMClientUtils clientUtils = new AGMClientUtils();

            final Client simpleClient = clientUtils.setupClient(new Client(values.get(AgmConstants.KEY_BASE_URL)), values);
            final Client entitiesClient = clientUtils.setupClient(new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)),simpleClient.getCookies(),values);
            List<Domain> domains = entitiesClient.getDomains();

            TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
            TimeSheet timeSheet = context.currentTimeSheet();

            final Date startDate = timeSheet.getPeriodStartDate().toGregorianCalendar().getTime();
            final Date endDate = timeSheet.getPeriodEndDate().toGregorianCalendar().getTime();

            for(final Domain d : domains){
                for(final Project p : simpleClient.getProjects(d.name).getCollection()){
                    Entities releases =entitiesClient.getCurrentReleases(d.name, p.name);
                    List<EntityComplexType> sprints = entitiesClient.getSprints(d.name, p.name);
                    List<EntityComplexType> tasks = entitiesClient.getCompletedTasks(d.name, p.name, values.get(AgmConstants.KEY_USERNAME));
                    for(EntityComplexType release : releases.getEntity()) {

                        EntityComplexTypeReader r = new EntityComplexTypeReader(release);
                        System.out.println("---------------------------------release");
                        System.out.println("release:" + r.toString());
                        for(EntityComplexType sprint:sprints) {
                            System.out.println("---------------------------------sprint");
                            EntityComplexTypeReader s = new EntityComplexTypeReader(sprint);
                            System.out.println("sprint:" + s.toString());
                        }
                        for(EntityComplexType task:tasks) {
                            System.out.println("---------------------------------task");
                            EntityComplexTypeReader t = new EntityComplexTypeReader(task);
                            System.out.println("task:" + t.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}