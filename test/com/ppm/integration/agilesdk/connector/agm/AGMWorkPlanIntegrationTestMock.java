package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.hp.ppm.integration.model.WorkplanMapping;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;
import com.ppm.integration.agilesdk.pm.ExternalWorkPlan;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext;
import com.ppm.integration.agilesdk.ui.DynamicDropdown;
import com.ppm.integration.agilesdk.ui.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;

import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.hp.ppm.integration.service.impl.WorkPlanIntegrationContextImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { AGMWorkPlanIntegration.class})
public class AGMWorkPlanIntegrationTestMock {

    ValueSet values = new ValueSet();
    Client mclient=null;
    @Before
    public void setUp() throws Exception {
        mclient=new MockClient("https://agilemanager-ast.saas.hp.com");
        values = CommonParameters.getDefaultValueSet();
    }

    @Test
    public void getMappingConfigurationFieldsTest() throws Exception {

        PowerMockito.whenNew(Client.class).withAnyArguments().thenReturn(mclient);

        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        long taskId = 115;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(taskId);

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
    public void getCustomDetailPageTest() throws Exception {
        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        String str=wpi.getCustomDetailPage();
        Assert.assertNotNull(str);

    }

    @Test
    public void linkTaskWithExternalTest() throws Exception {
        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        WorkplanMapping temp=wpi.linkTaskWithExternal(null, null, null);
        Assert.assertNull(temp);
    }

    @Test
    public void unlinkTaskWithExternalTest() throws Exception {
        AGMWorkPlanIntegration wpi = new AGMWorkPlanIntegration();
        wpi.unlinkTaskWithExternal(null, null, null);
        // No exception = success.
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getExternalWorkPlanTest() throws Exception {

        PowerMockito.whenNew(EntitiesClient.class).withAnyArguments().thenReturn((EntitiesClient)mclient);

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
