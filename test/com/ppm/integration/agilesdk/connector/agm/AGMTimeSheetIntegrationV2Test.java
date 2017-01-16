package com.ppm.integration.agilesdk.connector.agm;

import com.hp.ppm.tm.model.TimeSheet;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.AccessToken;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.ClientPublicAPI;
import com.ppm.integration.agilesdk.connector.agm.client.publicapi.TimesheetItem;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.tm.ExternalWorkItem;
import com.ppm.integration.agilesdk.ui.Field;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by libingc on 3/14/2016.
 */
public class AGMTimeSheetIntegrationV2Test {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Client entitiesClient = null;

    ValueSet values = new ValueSet();

    @Before public void setUp() throws Exception {
        values = CommonParameters.getDefaultValueSet();
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testGetMappingConfigurationFields() {
        AGMTimeSheetIntegrationV2 tsv2 = new AGMTimeSheetIntegrationV2();
        List<Field> fields = tsv2.getMappingConfigurationFields(values);
        Assert.assertNotNull(fields);
        int expectedSize = 2;
        Assert.assertEquals(expectedSize, fields.size());
    }

    @Test public void testGetExternalWorkItemsByTimesheets() throws Exception {
        TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
        AGMTimeSheetIntegrationV2 tsv2 = new AGMTimeSheetIntegrationV2();
        List<ExternalWorkItem> list = tsv2.getExternalWorkItems(context, values);
        Assert.assertNotNull(list);
        for (ExternalWorkItem item : list) {
            System.out.println("name=" + item.getName() + ", effort=" + item.getTotalEffort() + ", ExternalData=" + item
                    .getEffortBreakDown().toJsonString());
        }
    }

    @Test public void testGetTimeSheet() {
        try {
            AGMClientUtils clientUtils = new AGMClientUtils();

            final Client simpleClient =
                    clientUtils.setupClient(new Client(values.get(AgmConstants.KEY_BASE_URL)), values);
            final Client entitiesClient = clientUtils
                    .setupClient(new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)), simpleClient.getCookies(),
                            values);

            List<Domain> domains = entitiesClient.getDomains();

            TestTimeSheetIntegrationContext context = new TestTimeSheetIntegrationContext();
            TimeSheet timeSheet = context.currentTimeSheet();

            final Date startDate = timeSheet.getPeriodStartDate().toGregorianCalendar().getTime();
            final Date endDate = timeSheet.getPeriodEndDate().toGregorianCalendar().getTime();

            for (final Domain d : domains) {
                for (final Project p : simpleClient.getProjects(d.name).getCollection()) {
                    Entities releases = entitiesClient.getCurrentReleases(d.name, p.name);
                    List<EntityComplexType> sprints = entitiesClient.getSprints(d.name, p.name);

                    ClientPublicAPI client = clientUtils.setupClientPublicAPI(values);
                    String clientId = values.get(AgmConstants.APP_CLIENT_ID);
                    String clientSecret = values.get(AgmConstants.APP_CLIENT_SECRET);
                    AccessToken token = client.getAccessTokenWithFormFormat(clientId, clientSecret);
                    int workspaceId = values.getInteger("workspace", 0);
                    List<TimesheetItem> timeSheets =
                            client.getTimeSheetData(token, values.get(AgmConstants.KEY_USERNAME),
                                    dateFormat.format(startDate), dateFormat.format(endDate), workspaceId);

                    for (EntityComplexType release : releases.getEntity()) {

                        EntityComplexTypeReader r = new EntityComplexTypeReader(release);
                        System.out.println("---------------------------------release");
                        System.out.println("release:" + r.toString());
                        for (EntityComplexType sprint : sprints) {
                            System.out.println("---------------------------------sprint");
                            EntityComplexTypeReader s = new EntityComplexTypeReader(sprint);
                            System.out.println("sprint:" + s.toString());
                        }
                        for (TimesheetItem t : timeSheets) {
                            System.out.println("---------------------------------task");
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