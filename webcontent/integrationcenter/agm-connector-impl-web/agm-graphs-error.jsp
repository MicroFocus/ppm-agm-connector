<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"
    isErrorPage="true"
    trimDirectiveWhitespaces="true" %>

<%@ page import="com.ppm.integration.agilesdk.connector.agm.client.FieldQuery
                ,com.ppm.integration.agilesdk.connector.agm.client.GraphClient
                ,com.ppm.integration.agilesdk.connector.agm.client.ValueQuery
                ,com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult
                ,com.hp.ppm.integration.sdk.JsonUtils
                ,com.ppm.integration.agilesdk.ValueSet
                ,com.ppm.integration.agilesdk.pm.JspConstants
                ,com.ppm.integration.agilesdk.connector.agm.AgmConstants
                ,com.ppm.integration.IntegrationException
                ,com.ppm.integration.agilesdk.connector.agm.AGMConnectivityExceptionHandler" %>

<%@ include file="/integrationcenter/sdk/include-workplan-integration.jsp" %>
<%
    ValueSet values = (ValueSet) request.getAttribute(JspConstants.WORKPLAN_INTEGRATION_VALUE_SET);
%>
<%
    try{
        new AGMConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), exception);
    }catch(IntegrationException e){
%>
        <%=e.toString()%>
<%
    }
%>