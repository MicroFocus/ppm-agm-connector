package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.AuditComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Audits;
import com.mercury.itg.core.user.model.User;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.util.List;

/**
 * Created by libingc on 4/7/2016.
 */
public class AuditsTest {

    Client client = null;
    ValueSet values = new ValueSet();

    @Before
    public void setUp() throws Exception {
    	values = CommonParameters.getDefaultValueSet();
        Client aClient = new Client(values.get(AgmConstants.KEY_BASE_URL));
        User _currentUser = new TestUserImpl();
        aClient.setCurrentUser(_currentUser);

        client = new AGMClientUtils().setupClient(aClient, null, values);
    }


    @Test
    public void testXML2bean() throws Exception {
        String xml = "<Audits TotalResults=\"1\">\n" +
                "<Audit>\n" +
                "<Id>359906</Id>\n" +
                "<Action>UPDATE</Action>\n" +
                "<ParentId>28536</ParentId>\n" +
                "<ParentType>project-task</ParentType>\n" +
                "<Time>2015-12-30 23:54:36</Time>\n" +
                "<User>bing-chen.li@hpe.com</User>\n" +
                "<Properties>\n" +
                "\t<Property Label=\"Task Status\" Name=\"Task Status\">\n" +
                "\t\t<NewValue>Completed</NewValue>\n" +
                "\t\t<OldValue>New</OldValue>\n" +
                "\t</Property>\n" +
                "\t<Property Label=\"Task Status2\" Name=\"Task Status2\">\n" +
                "\t\t<NewValue>Completed2</NewValue>\n" +
                "\t\t<OldValue>New2</OldValue>\n" +
                "\t</Property>\n" +
                "</Properties>\n" +
                "</Audit>\n" +

                "<Audit>\n" +
                "<Id>3599069</Id>\n" +
                "<Action>UPDATE3</Action>\n" +
                "<ParentId>285363</ParentId>\n" +
                "<ParentType>project-task3</ParentType>\n" +
                "<Time>2015-12-30 23:54:36</Time>\n" +
                "<User>bing-chen.li@hpe.com</User>\n" +
                "<Properties>\n" +
                "\t<Property Label=\"Task Status\" Name=\"Task Status\">\n" +
                "\t\t<NewValue>Completed</NewValue>\n" +
                "\t\t<OldValue>New</OldValue>\n" +
                "\t</Property>\n" +
                "\t<Property Label=\"Task Status2\" Name=\"Task Status2\">\n" +
                "\t\t<NewValue>Completed2</NewValue>\n" +
                "\t\t<OldValue>New2</OldValue>\n" +
                "\t</Property>\n" +
                "</Properties>\n" +
                "</Audit>\n" +

                "</Audits>";

        Audits a = converyToJavaBean(xml, Audits.class);
        System.out.println(a.getTotalResults());
        for(AuditComplexType ad : a.getAudit()) {
            System.out.println(ad.getId());
            System.out.println(ad.getAction());
            System.out.println(ad.getParentId());
            System.out.println(ad.getParentType());
            System.out.println(ad.getTime());
            System.out.println(ad.getUser());
            List<AuditComplexType.Properties.Property> props = ad.getProperties().getProperty();
            for(AuditComplexType.Properties.Property p : props) {
                System.out.println("   " + p.getName());
                System.out.println("   " + p.getLabel());
                System.out.println("   " + p.getNewValue());
                System.out.println("   " + p.getOldValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
	public static <T> T converyToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }




    @Test
    public void getTaskAudits() {
        Audits audits = client.getTaskAudits(values.get(AgmConstants.KEY_DOMAIN),
                values.get(AgmConstants.KEY_PROJECT), "117");

        for(AuditComplexType audit : audits.getAudit()) {
            System.out.println(audit.getId());
            System.out.println(audit.getAction());
            System.out.println(audit.getParentId());
            System.out.println(audit.getParentType());
            System.out.println(audit.getTime());
            System.out.println(audit.getUser());
            AuditComplexType.Properties ps = audit.getProperties();

            for(AuditComplexType.Properties.Property p : ps.getProperty()) {
                System.out.println(p.getName());
                System.out.println(p.getLabel());
                System.out.println(p.getNewValue());
                System.out.println(p.getOldValue());
            }
        }
    }
}