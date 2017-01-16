package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.integration.model.WorkplanMapping;
import com.ppm.integration.IntegrationException;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.SprintDurationUnitsEnum;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldComplexType;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.hp.ppm.integration.service.impl.WorkPlanIntegrationContextImpl;
import com.mercury.itg.core.user.model.User;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;
import com.ppm.integration.agilesdk.pm.ExternalWorkPlan;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext;
import com.ppm.integration.agilesdk.ui.DynamicDropdown;
import com.ppm.integration.agilesdk.ui.Field;
import org.junit.*;

import java.util.List;

/**
 * Created by libingc on 2/25/2016.
 */
public class AGMWorkPlanIntegrationV2Test {

    AGMClientUtils integration = new AGMClientUtils();
    AGMWorkPlanIntegrationV2 workPlanTest = new AGMWorkPlanIntegrationV2();
    Client client = null;
    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();
        Client aClient = new Client(
                values.get(AgmConstants.KEY_BASE_URL));
        User _currentUser = new TestUserImpl();
        aClient.setCurrentUser(_currentUser);
        client = new AGMClientUtils().setupClient(aClient, null, values);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetTotalReleaseTask() {

    }

    @Test
    public void testGetCompletedTasks() throws Exception {
        String domainName = values.get(AgmConstants.KEY_DOMAIN);
        String projectName = values.get(AgmConstants.KEY_PROJECT);
        String userName = "sa";
        List<EntityComplexType> tasks = client.getCompletedTasks(domainName,
                projectName, userName);
        Assert.assertNotNull(tasks);
        for (EntityComplexType entity : tasks) {
            System.out.println("type: " + entity.getType() + ", fields: "
                    + entity.getFields());
            for (FieldComplexType fct : entity.getFields().getField()) {
                System.out.println("    name: " + fct.getName() + ", value: "
                        + fct.getValue());
                for (FieldComplexType.Value value : fct.getValue()) {
                    System.out.println("        value: " + value.getValue()
                            + ", alias: " + value.getAlias()
                            + ", referenceValue: " + value.getReferenceValue());
                }
            }
        }

    }

    @Test
    public void testGetMappingConfigurationFields() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        long taskId = 115;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                taskId);
        List<Field> fields = awpiv2.getMappingConfigurationFields(context,
                values);
        Assert.assertNotNull(fields);
        int expectedSize = 25;
        Assert.assertEquals(expectedSize, fields.size());
        AgmEntityDropdown aedd = (AgmEntityDropdown) fields.get(3);
        List<String> sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        List<DynamicDropdown.Option> options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(4);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(5);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(6);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(7);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(9);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
        aedd = (AgmEntityDropdown) fields.get(24);
        sList = aedd.getDependencies();
        Assert.assertNotNull(sList);
        options = aedd.fetchDynamicalOptions(values);
        Assert.assertNotNull(options);
    }

    @Test
    public void testLinkTaskWithExternal() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        long taskId = 152;
        WorkplanMapping mapping = new WorkplanMapping();
        mapping.setTaskId(taskId);
        WorkPlanIntegrationContextImpl context = new WorkPlanIntegrationContextImpl(115);
        mapping = awpiv2.linkTaskWithExternal(context, mapping, values);
        Assert.assertNotNull(mapping);
    }

    @Test
    @Ignore
    public void testLinkTaskWithExternalNewRelease() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        // long taskId = 115;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                115);
        ValueSet values1 = (ValueSet) values.clone();
        values1.put(AgmConstants.KEY_CREATE_RELEASE, "true");
        String newReleaseName = "LinkTest"
                + CommonParameters.genRandomNumber(4);
        values1.put(AgmConstants.KEY_NAME, newReleaseName);
        String releaseDesc = "Create release UT;";
        String startDate = "03/16/2016";
        String endDate = "04/30/2016";
        values1.put(AgmConstants.KEY_START_TIME, startDate);
        values1.put(AgmConstants.KEY_END_TIME, endDate);
        values1.put(AgmConstants.KEY_DESCRIPTION, releaseDesc);
        SprintDurationUnitsEnum sprintUnit = SprintDurationUnitsEnum.DAYS;
        values1.put(AgmConstants.KEY_SRRINT_DURATION_UNIT,
                sprintUnit.toString());
        WorkplanMapping mapping = awpiv2.linkTaskWithExternal(context, new WorkplanMapping(), values1);
        Assert.assertNotNull(mapping);
    }

    @Test
    public void testGetExternalWorkPlan() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                115);
        ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, values);
        Assert.assertNotNull(ewp);
    }

    @Test
    public void testGetExternalWorkPlanReleaseNotFoundError() throws Exception {
        try {
            AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
            WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                    115);
            ValueSet v = (ValueSet) values.clone();
            v.put(AgmConstants.KEY_RELEASE, "10");
            ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v);
            Assert.assertTrue(false);
        } catch (AgmClientException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        } catch (IntegrationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetExternalWorkPlanReleaseIsFinishedError()
            throws Exception {
        try {
            AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
            WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                    115);
            ValueSet v = (ValueSet) values.clone();
            v.put(AgmConstants.KEY_RELEASE, "1010");
            ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v);
            Assert.assertTrue(false);
        } catch (AgmClientException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        } catch (IntegrationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetExternalWorkPlanNoSprintId() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                115);
        ValueSet v = (ValueSet) values.clone();
        v.put(AgmConstants.KEY_SPRINT, "");
        ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v);
        Assert.assertNotNull(ewp);

    }

    @Test
    public void testGetExternalWorkPlanDetailRelease() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                115);
        ValueSet v = (ValueSet) values.clone();
        v.put(AgmConstants.KEY_DATA_DETAIL_LEVEL, AgmConstants.DETAILS_RELEASE);
        ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v);
        Assert.assertNotNull(ewp);
        List<ExternalTask> tasks = ewp.getRootTasks();
        Assert.assertNotNull(tasks);
        for (ExternalTask task : tasks) {
            System.out.println("id=" + task.getId() + ", name="
                    + task.getName() + ", status=" + task.getStatus()
                    + ", ownerid=" + task.getOwnerId() + ", ownerRole="
                    + task.getOwnerRole() + ", start="
                    + task.getScheduledStart() + ", finish="
                    + task.getScheduledFinish());
            List<ExternalTaskActuals> actuals = task.getActuals();
            Assert.assertNotNull(actuals);
            Assert.assertNull(task.getChildren());
        }
    }

    @Test
    public void testGetExternalWorkPlanDetailSprint() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                115);
        ValueSet v = (ValueSet) values.clone();
        v.put(AgmConstants.KEY_DATA_DETAIL_LEVEL, AgmConstants.DETAILS_SPRINT);
        ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v);
        Assert.assertNotNull(ewp);
        List<ExternalTask> tasks = ewp.getRootTasks();
        for (ExternalTask task : tasks) {
            System.out.println("id=" + task.getId() + ", name="
                    + task.getName() + ", status=" + task.getStatus()
                    + ", ownerid=" + task.getOwnerId() + ", ownerRole="
                    + task.getOwnerRole() + ", start="
                    + task.getScheduledStart() + ", finish="
                    + task.getScheduledFinish());
            List<ExternalTaskActuals> actuals = task.getActuals();
            Assert.assertNotNull(actuals);
            for (ExternalTaskActuals actual : actuals) {
                System.out.println(", efforts=" + actual.getActualEffort()
                        + ", complete percent=" + actual.getPercentComplete()
                        + ", schedule efforts=" + actual.getScheduledEffort()
                        + ", start=" + actual.getActualStart() + ", finish="
                        + actual.getActualFinish());
            }
        }
    }

}