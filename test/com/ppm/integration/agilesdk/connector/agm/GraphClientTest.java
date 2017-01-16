package com.ppm.integration.agilesdk.connector.agm;

import java.util.List;

import com.ppm.integration.agilesdk.ValueSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ppm.integration.agilesdk.connector.agm.client.FieldQuery;
import com.ppm.integration.agilesdk.connector.agm.client.GraphClient;
import com.ppm.integration.agilesdk.connector.agm.client.ValueQuery;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Attribute;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Column;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Columns;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.DataTable;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.MetadataAttributes;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Row;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Rows;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Values;

public class GraphClientTest {
	ValueSet values = new ValueSet();
	GraphClient graphClient;
	int release = 1006;
	String baseURL;
	String domain;
	String project;

	@Before
	public void setUp() throws Exception {
		values = CommonParameters.getDefaultValueSet();
		baseURL = values.get(AgmConstants.KEY_BASE_URL);
		domain = values.get(AgmConstants.KEY_DOMAIN);
		project = values.get(AgmConstants.KEY_PROJECT);
	}

	@Test
	public void testFeatureStatus() {
		String graphName = "Feature Status";
		graphClient = new GraphClient(baseURL, domain, project, graphName);
        new AGMClientUtils().setupClient(graphClient, values);
		GraphResult result = graphClient.getGraphResult(new FieldQuery(
				"release_id", ValueQuery.val(release + "")), new FieldQuery(
				"team_id", ValueQuery.val("-2")));
		Assert.assertNotNull(result);
		System.out.println("date=" + result.getDate() + ", data table="
				+ result.getDataTable() + ", errors=" + result.getErrors());
	}

	@Test
	public void testSprintBurnDown() {
		String graphName = "Sprint Burn Down";
		graphClient = new GraphClient(baseURL, domain, project, graphName);
        new AGMClientUtils().setupClient(graphClient, values);
		GraphResult result = graphClient.getGraphResult(new FieldQuery(
				"release_id", ValueQuery.val(release + "")), new FieldQuery(
				"team_id", ValueQuery.val("-2")));
		Assert.assertNotNull(result);
		System.out.println("date=" + result.getDate() + ", data table="
				+ result.getDataTable() + ", errors=" + result.getErrors());
		DataTable table = result.getDataTable();
		Columns columns = table.getColumns();
		List<Column> column = columns.getColumn();
		for (Column c : column) {
			System.out.println("column info: name:" + c.getName());
		}
		Rows rows = table.getRows();
		List<Row> row = rows.getRow();
		for (Row r : row) {
			System.out.println("row info: name: " + r.getName() + ", values: "
					+ r.getValues());
			Values values = r.getValues();
			List<String> value = values.getValue();
			for (String v : value) {
				System.out.println("values: " + v);
			}
		}
		MetadataAttributes meta = table.getMetadataAttributes();
		List<Attribute> attrs = meta.getAttribute();
		for (Attribute attr : attrs) {
			System.out.println("attr info: name=" + attr.getName() + ", desc="
					+ attr.getDescription() + ", value=" + attr.getValue());
		}
	}

	@Test
	public void testReleaseBurnup() {
		String graphName = "Release Burn Up";
		graphClient = new GraphClient(baseURL, domain, project, graphName);
        new AGMClientUtils().setupClient(graphClient, values);
		GraphResult result = graphClient.getGraphResult(new FieldQuery(
				"release_id", ValueQuery.val(release + "")), new FieldQuery(
				"team_id", ValueQuery.val("-2")));
		Assert.assertNotNull(result);
		System.out.println("date=" + result.getDate() + ", data table="
				+ result.getDataTable() + ", errors=" + result.getErrors());
	}

	@Test
	public void testThemeStatus() {
		String graphName = "Theme Status";
		graphClient = new GraphClient(baseURL, domain, project, graphName);
        new AGMClientUtils().setupClient(graphClient, values);
		GraphResult result = graphClient.getGraphResult(new FieldQuery(
				"release_id", ValueQuery.val(release + "")), new FieldQuery(
				"team_id", ValueQuery.val("-2")));
		Assert.assertNotNull(result);
		System.out.println("date=" + result.getDate() + ", data table="
				+ result.getDataTable() + ", errors=" + result.getErrors());
	}

}
