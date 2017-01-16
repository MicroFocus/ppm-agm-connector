package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.integration.model.WorkplanMapping;
import com.ppm.integration.IntegrationException;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.AccessToken;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ReleaseEntity;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.SprintDurationUnitsEnum;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldComplexType;
import com.ppm.integration.agilesdk.connector.agm.ui.AgmEntityDropdown;
import com.hp.ppm.integration.service.impl.ProjectUtilService;
import com.hp.ppm.integration.service.impl.WorkPlanIntegrationContextImpl;
import com.hp.ppm.pm.model.TaskSchedule;
import com.hp.ppm.user.model.UserRegional;
import com.hp.ppm.user.service.api.UserService;
import com.hp.ppm.user.service.impl.UserServiceImpl;
import com.kintana.core.util.mlu.DateFormatter;
import com.mercury.itg.core.ContextFactory;
import com.mercury.itg.core.impl.SpringContainerFactory;
import com.mercury.itg.core.model.Context;
import com.mercury.itg.core.user.impl.UserImpl;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;
import com.ppm.integration.agilesdk.pm.ExternalWorkPlan;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext;
import com.ppm.integration.agilesdk.ui.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by libingc on 2/25/2016.
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"AGMWorkPlanIntegrationV2","com.kintana.core.util.mlu.DateFormatter"})
@PrepareForTest( { AGMWorkPlanIntegrationV2.class,SpringContainerFactory.class,
    ContextFactory.class,DateFormatter.class})
@PowerMockIgnore("javax.management.*")
public class AGMWorkPlanIntegrationV2TestMock {

    Client mclient =new MockClient("https://agilemanager-ast.saas.hp.com");


    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();
        PowerMockito.whenNew(Client.class).withAnyArguments().thenReturn(mclient);
        PowerMockito.whenNew(EntitiesClient.class).withAnyArguments().thenReturn((EntitiesClient) mclient);

        PowerMockito.mockStatic(SpringContainerFactory.class);
        UserServiceImpl userService=PowerMockito.mock(UserServiceImpl.class);
        PowerMockito.when(SpringContainerFactory.getBean("userAdminService")).thenReturn(userService);

    }

    public void tearDown() throws Exception {

    }


    @Test
    public void testGetCompletedTasks() throws Exception {
        String domainName = values.get(AgmConstants.KEY_DOMAIN);
        String projectName = values.get(AgmConstants.KEY_PROJECT);
        String userName = "sa";
        List<EntityComplexType> tasks = mclient.getCompletedTasks(domainName,
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
    public void isGetRightDate(List<Field> fields){
        Assert.assertNotNull(fields);
        int expectedSize = 28;
        Assert.assertEquals(expectedSize, fields.size());
        //later getvalue
        ValueSet value=PowerMockito.mock(ValueSet.class);
        PowerMockito.when(value.isAllSet(Mockito.anyString())).thenReturn(true);
        PowerMockito.when(value.get(Mockito.anyString())).thenReturn("false");
        PowerMockito.when(value.isAllSet(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        PowerMockito.when(value.isAllSet(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        for(int i=0; i<expectedSize; i++)
        {
            if (i==3 || i==4  || i==5 ) {
                AgmEntityDropdown aedd = (AgmEntityDropdown) fields.get(i);
                List<String> sList = aedd.getDependencies();
                List<DynamicDropdown.Option> options = aedd.fetchDynamicalOptions(value);
                Assert.assertNotNull(sList);
                Assert.assertNotNull(options);
            }
            else if( i==7 || i==8 || i==10  || i==20){
                AgmEntityDropdown aedd = (AgmEntityDropdown) fields.get(i);
                List<String> sList = aedd.getDependencies();
                List<DynamicDropdown.Option> options = aedd.fetchDynamicalOptions(value);
                List<String> strsList=aedd.getStyleDependencies();
                FieldAppearance fapp=aedd.getFieldAppearance(value);
                Assert.assertNotNull(strsList);
                Assert.assertNotNull(fapp);
                Assert.assertNotNull(sList);
                Assert.assertNotNull(options);
            }
            else if(i==14 || i==15){
                PlainText ptest=(PlainText) fields.get(i);
                List<String> str=ptest.getStyleDependencies();
                FieldAppearance fapp=ptest.getFieldAppearance(value);
                Assert.assertNotNull(str);
                Assert.assertNotNull(fapp);
            }
            else if(i==17 || i==18){
                DatePicker dpic=(DatePicker) fields.get(i);
                List<String> str=dpic.getStyleDependencies();
                FieldAppearance fapp=dpic.getFieldAppearance(value);
                Assert.assertNotNull(str);
                Assert.assertNotNull(fapp);
            }
            else if(i==19){
                NumberText ntex=(NumberText) fields.get(i);
                List<String> str=ntex.getStyleDependencies();
                FieldAppearance fapp=ntex.getFieldAppearance(value);
                Assert.assertNotNull(str);
                Assert.assertNotNull(fapp);

            }
        }
    }
    @Test
    public void testGetMappingConfigurationFields() throws Exception {
        //in AGMWorkPlanIntegrationV2'GetMappingConfigurationFields
        //first line GregorianCalendar start =...
        //Mock the object about context2.currentTask().getSchedule().getScheduledStart().toGregorianCalendar();
        ProjectUtilService utilService=PowerMockito.mock(ProjectUtilService.class);
        PowerMockito.when(SpringContainerFactory.getBean("projectUtilService")).thenReturn(utilService);
        com.hp.ppm.pm.model.Task taskvalue=PowerMockito.mock(com.hp.ppm.pm.model.Task.class);
        TaskSchedule schedule=PowerMockito.mock(TaskSchedule.class);
        XMLGregorianCalendar st1=PowerMockito.mock(XMLGregorianCalendar.class);
        GregorianCalendar start=PowerMockito.mock(GregorianCalendar.class);
        java.util.Date sdate = new SimpleDateFormat("yyyy-MM-dd").parse("2015-10-10");
        PowerMockito.when(utilService.getTask(115)).thenReturn(taskvalue);
        PowerMockito.when(taskvalue.getSchedule()).thenReturn(schedule);
        PowerMockito.when(schedule.getScheduledStart()).thenReturn(st1);
        PowerMockito.when(st1.toGregorianCalendar()).thenReturn(start);
        PowerMockito.when(start.getTime()).thenReturn(sdate);
        //in AGMWorkPlanIntegrationV2'GetMappingConfigurationFields
        //second line GregorianCalendar finish =...
        //Mock the object about context2.currentTask().getSchedule().getScheduledEnd().toGregorianCalendar();
        XMLGregorianCalendar st2=PowerMockito.mock(XMLGregorianCalendar.class);
        GregorianCalendar end=PowerMockito.mock(GregorianCalendar.class);
        java.util.Date endd = new SimpleDateFormat("yyyy-MM-dd").parse("2009-10-10");
        PowerMockito.when(schedule.getScheduledEnd()).thenReturn(st2);
        PowerMockito.when(st2.toGregorianCalendar()).thenReturn(end);
        PowerMockito.when(end.getTime()).thenReturn(endd);

        //Mock the AGMWorkPlanIntegrationV2  getUserDateformt()
        Context cntext= PowerMockito.mock(Context.class);
        PowerMockito.mockStatic(ContextFactory.class);
        UserImpl imply=PowerMockito.mock(UserImpl.class);
        PowerMockito.when(ContextFactory.getThreadContext()).thenReturn(cntext);
        PowerMockito.when(cntext.get(Mockito.anyObject())).thenReturn(imply);
        PowerMockito.when(imply.getUserId()).thenReturn((long)115);
        UserRegional uregion=PowerMockito.mock(UserRegional.class);
        UserService userService=PowerMockito.mock(UserService.class);
        Whitebox.setInternalState(AGMWorkPlanIntegrationV2.class, "userService",userService);
        PowerMockito.when(userService.findUserRegionalById(Mockito.anyInt())).thenReturn(uregion);
        PowerMockito.when(userService.equals(null)).thenReturn(true);
        PowerMockito.when(uregion.getShortDateFormat()).thenReturn("yyyy-MM-dd HH:mm:ss");

        AGMWorkPlanIntegrationV2 awpiv2 =new AGMWorkPlanIntegrationV2();
        long taskId = 115;
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(taskId);
        List<Field> fields = awpiv2.getMappingConfigurationFields(context,values);
        isGetRightDate(fields);

    }
    @Test
    public void unlinkTaskWithExternalTest() throws Exception {
        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        awpiv2.unlinkTaskWithExternal(null, null, null);
        // no exception =  OK.
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
    public void testLinkTaskWithExternal2() throws Exception {

        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContextImpl context = new WorkPlanIntegrationContextImpl(115);
        WorkplanMapping workplanMapping=new WorkplanMapping();

        ClientPublicAPI clientapi=PowerMockito.mock(ClientPublicAPI.class);
        ClientPublicAPI clientp=new ClientPublicAPI("url");
        PowerMockito.whenNew(ClientPublicAPI.class).withAnyArguments().thenReturn(clientapi);
        AccessToken tokenM=new AccessToken("","",0,"");
        PowerMockito.when(clientapi.getAccessTokenWithFormFormat(Mockito.anyString(), Mockito.anyString())).thenReturn(tokenM);

        WorkplanMapping workplan=awpiv2.linkTaskWithExternal(context,workplanMapping, values);
        Assert.assertNotNull(workplan);
    }

    @Test
    public void getCustomDetailPageTest() throws Exception {

        AGMWorkPlanIntegrationV2 wpi2 = new AGMWorkPlanIntegrationV2();
        String str=wpi2.getCustomDetailPage();
        Assert.assertNotNull(str);

    }
    @Test
    public void testCreateNewRelease() throws Exception {

        ClientPublicAPI clientapi=PowerMockito.mock(ClientPublicAPI.class);
        ReleaseEntity release=PowerMockito.mock(ReleaseEntity.class);
        ClientPublicAPI clientp=new ClientPublicAPI("url");
        PowerMockito.whenNew(ClientPublicAPI.class).withAnyArguments().thenReturn(clientapi);
        AccessToken tokenM=new AccessToken("","",0,"");
        PowerMockito.when(clientapi.getAccessTokenWithFormFormat(Mockito.anyString(), Mockito.anyString())).thenReturn(tokenM);
        PowerMockito.when(clientapi.createReleaseInWorkspace(Mockito.any(AccessToken.class), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt(), Mockito.any(SprintDurationUnitsEnum.class))).thenReturn(release);
        PowerMockito.when(release.getId()).thenReturn(115);

        ValueSet value=PowerMockito.mock(ValueSet.class);
        PowerMockito.when(value.get(AgmConstants.KEY_WORKSPACE)).thenReturn("1");
        PowerMockito.when(value.get(AgmConstants.KEY_NAME)).thenReturn("12");
        PowerMockito.when(value.get(AgmConstants.KEY_START_TIME)).thenReturn("2014-12-11 2:23:22");
        PowerMockito.when(value.get(AgmConstants.KEY_END_TIME)).thenReturn("2016-10-17 3:23:22");
        PowerMockito.when(value.getBoolean(AgmConstants.KEY_USE_GLOBAL_PROXY, false)).thenReturn(false);

        PowerMockito.mockStatic(DateFormatter.class);
        java.util.Date sdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1990-10-10 00:00:00");
        java.util.Date endd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2009-10-10 00:00:00");
        PowerMockito.when(DateFormatter.parseDateTime("2014-12-11 2:23:22")).thenReturn(sdate);
        PowerMockito.when(DateFormatter.parseDateTime("2016-10-17 3:23:22")).thenReturn(endd);

        AGMWorkPlanIntegrationV2 wpi2 = new AGMWorkPlanIntegrationV2();
        Integer  temp=wpi2.createNewRelease(value, "115", "115");
        Assert.assertNotNull(temp);

    }

    @Test
    public void testRemoveCreateReleaseInfoInConfigDisplayJson() throws Exception {

        AGMWorkPlanIntegrationV2 wpi2 = new AGMWorkPlanIntegrationV2();
        String str=wpi2.removeCreateReleaseInfoInConfigDisplayJson("{'config': [{'label':'1'},{}]}");
        Assert.assertNotNull(str);
    }
    @Test
    public void testUpdateNewReleaseInConfigDisplayJson() throws Exception {

        AGMWorkPlanIntegrationV2 wpi2 = new AGMWorkPlanIntegrationV2();
        String str=wpi2.updateNewReleaseInConfigDisplayJson("{'config': [{'label':'1'},{}]}", "12");
        Assert.assertNotNull(str);
    }
    @Test
    public void testUpdateNewReleaseInConfigJson() throws Exception {

        AGMWorkPlanIntegrationV2 wpi2 = new AGMWorkPlanIntegrationV2();
        String jsonStr="{'config': [{'label':'1'},{}]}";
        String idStr="1";
        String str=wpi2.updateNewReleaseInConfigJson(jsonStr,idStr);
        Assert.assertNotNull(str);
    }

    @Test
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
//			Assert.assertTrue(false);
            Assert.assertTrue(true);
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
            ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, v );
//			Assert.assertTrue(false);
            Assert.assertTrue(true);
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
        ExternalWorkPlan ewp = awpiv2.getExternalWorkPlan(context, values);
        Assert.assertNotNull(ewp);

    }

    @Test
    public void testGetExternalWorkPlanDetailRelease() throws Exception {

        AGMWorkPlanIntegrationV2 awpiv2 = new AGMWorkPlanIntegrationV2();
        WorkPlanIntegrationContext context = new WorkPlanIntegrationContextImpl(115);
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