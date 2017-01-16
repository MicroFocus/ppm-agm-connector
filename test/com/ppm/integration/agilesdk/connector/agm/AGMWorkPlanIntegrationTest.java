package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.integration.service.impl.WorkPlanIntegrationContextImpl;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;
import com.ppm.integration.agilesdk.pm.ExternalWorkPlan;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext;
import com.ppm.integration.agilesdk.ui.DynamicDropdown;
import com.ppm.integration.agilesdk.ui.Field;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class AGMWorkPlanIntegrationTest {

    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();
    }

    @Test
    public void getMappingConfigurationFieldsTest() throws Exception {
        AGMClientUtils clientUtils = new AGMClientUtils();

        Client simpleClient = clientUtils.setupClient(new Client(
                values.get(AgmConstants.KEY_BASE_URL)), values);
        Client client = clientUtils.setupClient(new EntitiesClient(
                values.get(AgmConstants.KEY_BASE_URL)), simpleClient
                .getCookies(), values);
        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        long taskId = 115;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                taskId);
        List<Field> fields = wpi.getMappingConfigurationFields(context, values);
        Assert.assertNotNull(fields);
        int expectedSize = 11;
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
    }

    @Test
    public void getExternalWorkPlanTest() throws Exception {
        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        long taskId = 0;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(
                taskId);
        ExternalWorkPlan ewp = wpi.getExternalWorkPlan(context, values);
        Assert.assertNotNull(ewp);
        List<ExternalTask> ets = ewp.getRootTasks();
        Assert.assertNotNull(ets);
        for (ExternalTask et : ets) {
            System.out.println("name=" + et.getName() + ", id=" + et.getId()
                    + ", owner role=" + et.getOwnerRole() + ", status="
                    + et.getStatus() + ", ScheduleStart="
                    + et.getScheduledStart() + ", ScheduleFinish="
                    + et.getScheduledFinish() + ", OwnerId=" + et.getOwnerId()
                    + ", Actuals=" + et.getActuals());
            List<ExternalTask> tasks = et.getChildren();
            if (tasks != null) {
                for (ExternalTask task : tasks) {
                    System.out.println("Sprint US information: name="
                            + task.getName() + ", id=" + task.getId()
                            + ", status=" + task.getStatus() + ", ownerid="
                            + task.getOwnerId() + ", ownerRole="
                            + task.getOwnerRole() + ", start="
                            + task.getScheduledStart() + ", finish="
                            + task.getScheduledFinish());
                    List<ExternalTaskActuals> actuals = task.getActuals();
                    if (actuals != null) {
                        for (ExternalTaskActuals actual : actuals) {
                            System.out.println("US Task actual information: "
                                    + "efforts=" + actual.getActualEffort()
                                    + ", complete percent=" + actual.getPercentComplete()
                                    + ", schedule efforts=" + actual.getScheduledEffort()
                                    + ", start=" + actual.getActualStart()
                                    + ", finish=" + actual.getActualFinish());
                        }
                    }
                }
            }
        }
    }
}
