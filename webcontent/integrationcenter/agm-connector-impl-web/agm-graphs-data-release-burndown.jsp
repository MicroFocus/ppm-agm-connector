<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page errorPage="agm-graphs-error.jsp" %>
<%@ page import="com.ppm.integration.agilesdk.connector.agm.client.FieldQuery
                ,com.ppm.integration.agilesdk.connector.agm.client.GraphClient
                ,com.ppm.integration.agilesdk.connector.agm.client.ValueQuery
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult
                ,com.hp.ppm.integration.sdk.JsonUtils
                ,com.ppm.integration.agilesdk.ValueSet
                ,com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext
                ,com.ppm.integration.agilesdk.pm.JspConstants
                ,com.ppm.integration.agilesdk.connector.agm.AgmConstants
                ,com.ppm.integration.agilesdk.connector.agm.AGMWorkPlanIntegration" %>

<%@ include file="/integrationcenter/sdk/include-workplan-integration.jsp" %>
<%
    final String graphName = "Release Burn Down";
%>
<%@ include file="agm-graphs-data.inc" %>
<%
	GraphResult result = graphClient.getGraphResult(
		new FieldQuery("release_id",ValueQuery.val(release+""))
		,new FieldQuery("team_id",ValueQuery.val("-2")));
%>
<%=JsonUtils.toJson(result)%>