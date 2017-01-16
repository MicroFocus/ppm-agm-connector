<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page errorPage="agm-graphs-error.jsp" %>
<%@ page import="java.text.SimpleDateFormat
                ,java.util.Date
                ,org.apache.commons.lang.StringUtils
                ,com.ppm.integration.agilesdk.connector.agm.client.FieldQuery
                ,com.ppm.integration.agilesdk.connector.agm.client.GraphClient
                ,com.ppm.integration.agilesdk.connector.agm.client.ValueQuery
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult
                ,com.hp.ppm.integration.sdk.JsonUtils
                ,com.ppm.integration.agilesdk.ValueSet
                ,com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext
                ,com.ppm.integration.agilesdk.pm.JspConstants
                ,com.ppm.integration.agilesdk.connector.agm.AgmConstants
                ,com.ppm.integration.agilesdk.connector.agm.client.Client
                ,com.ppm.integration.agilesdk.connector.agm.client.FieldQuery
                ,com.ppm.integration.agilesdk.connector.agm.AGMClientUtils
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType
                ,com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader" %>

<%@ include file="/integrationcenter/sdk/include-workplan-integration.jsp" %>
<%
    final String graphName = "Sprint Burn Down";
%>
<%@ include file="agm-graphs-data.inc" %>
<%
    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    Client client = new Client(baseUrl).auth(graphClient.getCookies());
    new AGMClientUtils().setupClient(client, values);
    Entities sprints = client.allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/release-cycles", domain, project)
				,new FieldQuery("start-date", ValueQuery.lte( today ))
				,new FieldQuery("end-date", ValueQuery.gte( today ))
				,new FieldQuery("parent-id",ValueQuery.val( release )))
			.get(Entities.class);

    String sprint = null;
	for(EntityComplexType e : sprints.getEntity()){
		EntityComplexTypeReader r = new EntityComplexTypeReader(e);
		sprint = r.strValue("id");
		break;
	}

    if(StringUtils.isEmpty(sprint)){
%>
<%="{\"ret\":\"404\",\"message\":[\"ERROR_CANNOT_GET_CURRENT_SPRINT\"],\"dataTable\":{\"metadataAttributes\":{\"attribute\":[{\"name\":\"GraphName\", \"value\":\"Sprint Burn Down\"}]}} }"%>
<%
    }else{
	    GraphResult result = graphClient.getGraphResult(
    		new FieldQuery("release_id",ValueQuery.val(release))
    		,new FieldQuery("sprint_id",ValueQuery.val(sprint))
    		,new FieldQuery("team_id",ValueQuery.val("-2")));
%>
<%=JsonUtils.toJson(result)%>
<%  }%>
