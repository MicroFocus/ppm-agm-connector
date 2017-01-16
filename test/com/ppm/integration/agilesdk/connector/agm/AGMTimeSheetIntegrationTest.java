package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.tm.model.TimeSheet;
import com.mercury.itg.core.user.model.User;
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

import java.util.Date;
import java.util.List;

/**
 * Created by libingc on 2/25/2016.
 */
public class AGMTimeSheetIntegrationTest {

    AGMClientUtils integration = new AGMClientUtils();
    AGMWorkPlanIntegrationV2 workPlanTest = new AGMWorkPlanIntegrationV2();
    Client entitiesClient = null;
    Client client = null;
    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();
        Client aClient = new Client(values.get(AgmConstants.KEY_BASE_URL));
        User _currentUser = new TestUserImpl();
        aClient.setCurrentUser(_currentUser);
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
    public void testGetExternalWorkItemsByTasks(){
        AGMTimeSheetIntegration asi = new AGMTimeSheetIntegration();
        TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
        List<ExternalWorkItem> items = asi.getExternalWorkItemsByTasks(context, values);
        Assert.assertNotNull(items);
        for (ExternalWorkItem item : items) {
            System.out.println("name=" + item.getName() + ", effort="
                    + item.getTotalEffort() + ", errorMessage=" + item.getErrorMessage()
                    /*+ ", ExternalData=" + item.getExternalData()*/);
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