package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.ppm.integration.agilesdk.ValueSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.Projects;
import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;

public class ClientTest {
	Client client = null;
	ValueSet values = new ValueSet();

	@Before
	public void setUp() throws Exception {
		values = CommonParameters.getDefaultValueSet();
		Client simpleClient = new AGMClientUtils().setupClient(new Client(
				values.get(AgmConstants.KEY_BASE_URL)), values);
		client = new AGMClientUtils().setupClient(
				new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)),
				simpleClient.getCookies(), values);
	}

	@Test
	@Ignore
	public void testGetDomainsByGlobalProxy() throws Exception {
		values = (ValueSet) CommonParameters.getDefaultValueSet().clone();
		values.put(AgmConstants.KEY_USE_GLOBAL_PROXY, "true");
		Client simpleClient1 = new AGMClientUtils().setupClient(new Client(
				values.get(AgmConstants.KEY_BASE_URL)), values);
		Client client1 = new AGMClientUtils().setupClient(
				new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)),
				simpleClient1.getCookies(), values);
		List<Domain> domains = client1.getDomains();
		Assert.assertNotNull(domains);
		Assert.assertTrue(domains.size() > 0);
		for (Domain d : domains) {
			System.out.println(d.name);
		}
	}

	@Test
	public void testGetDomains() throws Exception {
		List<Domain> domains = client.getDomains();
		Assert.assertNotNull(domains);
		Assert.assertTrue(domains.size() > 0);
		for (Domain d : domains) {
			System.out.println(d.name);
		}
	}
	
	@Test
	public void testGetProjectsByProxy() throws Exception {
		ValueSet values = (ValueSet) CommonParameters.getDefaultValueSet().clone();
		Client simpleClient1 = new AGMClientUtils().setupClient(new Client(
				values.get(AgmConstants.KEY_BASE_URL)), values);
		Client client1 = new AGMClientUtils().setupClient(
				new EntitiesClient(values.get(AgmConstants.KEY_BASE_URL)+"/"),
				simpleClient1.getCookies(), values);
		Projects prjs = client1.getProjects(values.get(AgmConstants.KEY_DOMAIN));
		for (Project p : prjs.getCollection()) {
			System.out.println(p.name);
		}
	}

	@Test
	public void testGetProjects() throws Exception {
		Projects prjs = client.getProjects(values.get(AgmConstants.KEY_DOMAIN));
		for (Project p : prjs.getCollection()) {
			System.out.println(p.name);
		}
	}

	@Test
	public void testGetWorkSpaces() throws Exception {
		Entities workspaces = client.getWorkSpaces(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT));
		for (EntityComplexType e : workspaces.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println(reader.strValue("name") + "         "
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetAllReleases() throws Exception {
		Entities release = client.getAllReleases(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_WORKSPACE));
		for (EntityComplexType e : release.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("name=" + reader.strValue("name") + ", id="
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetCurrentReleases() throws Exception {
		Entities release = client.getCurrentReleases(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_WORKSPACE));

		for (EntityComplexType e : release.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("name=" + reader.strValue("name") + ", id="
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetReleaseById() throws Exception {
		Entities release = client.getReleaseById(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_RELEASE));
		for (EntityComplexType e : release.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("name=" + reader.strValue("name") + ", id="
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetTasks() throws Exception {
		Entities usTask = client.getTasks(values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_RELEASE));
		for (EntityComplexType e : usTask.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("name=" + reader.strValue("name") + ", id="
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetUserStoryByReleaseBlockItem() {

		Entities entities = client.getUserStoryByReleaseBlockItem(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_RELEASE));
		Assert.assertNotNull(entities);
		for (EntityComplexType e : entities.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("name=" + reader.strValue("name") + ", id="
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetReleaseByParentId() throws Exception {
		Entities userStory = client.getSprintsByParentId(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_RELEASE));
		for (EntityComplexType e : userStory.getEntity()) {
			EntityComplexTypeReader r = new EntityComplexTypeReader(e);
			System.out.println(r.strValue("parent-id"));
		}
	}

	@Test
	public void testGetRelease() throws Exception {
		Entities release = client.getCurrentReleases(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT));
		for (EntityComplexType e : release.getEntity()) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println(reader.strValue("name") + "         "
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetSprints() {
		List<EntityComplexType> sprint = client.getSprints(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT));
		for (EntityComplexType e : sprint) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println(reader.strValue("name") + "         "
					+ reader.strValue("id"));
		}
	}

	@Test
	public void testGetCompletedTasks() {
		List<EntityComplexType> tasks = client.getCompletedTasks(
				values.get(AgmConstants.KEY_DOMAIN),
				values.get(AgmConstants.KEY_PROJECT),
				values.get(AgmConstants.KEY_USERNAME));
		for (EntityComplexType e : tasks) {
			EntityComplexTypeReader reader = new EntityComplexTypeReader(e);
			System.out.println("task ID:" + reader.strValue("id") + "   " + reader.strValue("name") + "   "  + reader.strValue("effort") + "   " );
//			System.out.println("task ID:" + reader.strValue("id"));
		}

	}

}
