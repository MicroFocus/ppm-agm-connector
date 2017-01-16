<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.ppm.integration.agilesdk.connector.agm.client.FieldQuery
                ,com.ppm.integration.agilesdk.connector.agm.client.GraphClient
                ,com.ppm.integration.agilesdk.connector.agm.client.ValueQuery
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult
                ,com.hp.ppm.integration.sdk.JsonUtils
                ,com.ppm.integration.agilesdk.ValueSet
                ,com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext
                ,com.ppm.integration.agilesdk.pm.JspConstants
                ,com.ppm.integration.agilesdk.connector.agm.AgmConstants
                ,com.ppm.integration.agilesdk.connector.agm.AGMClientUtils" %>

<%
    WorkPlanIntegrationContext wpContext = (WorkPlanIntegrationContext) request.getAttribute(JspConstants.WORKPLAN_INTEGRATION_CONTEXT);
    ValueSet values = (ValueSet) request.getAttribute(JspConstants.WORKPLAN_INTEGRATION_VALUE_SET);

    String domain  = values.get(AgmConstants.KEY_DOMAIN),
           project = values.get(AgmConstants.KEY_PROJECT),
           release = values.get(AgmConstants.KEY_RELEASE),
           baseUrl = values.get(AgmConstants.KEY_BASE_URL);

	GraphClient graphClient = new GraphClient(baseUrl,domain,project,graphName);
	new AGMClientUtils().setupClient(graphClient, values);
%>