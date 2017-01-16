package com.ppm.integration.agilesdk.connector.agm.client.publicapi;

import com.ppm.integration.agilesdk.connector.agm.client.AgmClientException;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Agile Manager public API
 * In order to run this example you must make sure you have access to the Agile Manager server and that you have created an API application
 * and has the correct API secret.
 */
public class ClientPublicAPI {
    
    private final Logger logger = Logger.getLogger(this.getClass());

    protected String baseURL = "https://agilemanager-ast.saas.hp.com";

    private Proxy proxy = null;

    public ClientPublicAPI(String baseUrl) {
        this.baseURL = baseUrl.trim();
        if(this.baseURL.endsWith("/")){
            this.baseURL = this.baseURL.substring(0, this.baseURL.length()-1);
        }
    }

    /**
     * Delete a given release using DELETE REST request
     * @param token the security token needed for every REST request
     * @param releaseId - the id of the release to be deleted
     * @throws IOException
     */

//    public void deleteRelease(AccessToken token, int workspaceId, int releaseId) throws IOException {
//        //http(s)://<server>:<port>/agm/api/workspaces/<workspace_id>/releases/<release_id>
//        String url=String.format("%s/agm/api/workspaces/%d/releases/%d", baseURL, workspaceId, releaseId);
//        Map<String, String> headers = createHeader(token);
//        RestResponse response = sendRequest(url, HttpMethod.DELETE, null, headers);
//        verifyResult(HttpStatus.SC_OK,response.getStatusCode());
//    }

    /**
     * Creates a new release starting today ending in 60 days, having a sprint every two weeks. Uses the POST REST request
     * @param token the security token needed for every REST request
     * @param workspaceId
     * @return the newly created release
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     */

    public ReleaseEntity createReleaseInWorkspace(AccessToken token, int workspaceId , String releaseName, String releaseDesc, String startDate, String endDate, int sprintDuration, SprintDurationUnitsEnum sprintUnit) throws IOException, JSONException, ParseException {
        ReleaseEntity result=null;
        ReleaseEntity newRelease=new ReleaseEntity();
        newRelease.setName(releaseName);
        newRelease.setDescription(releaseDesc);
        newRelease.setStartDate(startDate);
        newRelease.setEndDate(endDate);
        newRelease.setSprintDuration(sprintDuration);
        newRelease.setSprintDurationUnits(sprintUnit);
        //http(s)://<server>:<port>/agm/api/workspaces/<workspace_id>/releases
        String url=String.format("%s/agm/api/workspaces/%d/releases",baseURL,workspaceId);
        //{ "data":[{"name": "new release", "description": "new release description", "start_date": "2015-06-25", "end_date": "2015-08-24", "sprint_duration": "2", "sprint_duration_units" : "Weeks" }]}
        String body=newRelease.generateBody();
        Map<String, String> headers = createHeader(token);
        RestResponse response = sendRequest(url, HttpMethod.POST, body, headers);
        if (HttpStatus.SC_CREATED != response.getStatusCode())
        {
            this.logger.error("error in create release. response.getStatusCode()=" + response.getStatusCode());
            throw new AgmClientException("AGM_APP", "ERROR_HTTP_CONNECTIVITY_ERROR", new String[] { response.getData() });
        }
        result=ReleaseEntity.createFromJson(response.getData());
        return result;
    }

    public List<TimesheetItem> getTimeSheetData(AccessToken token, String userName, String startDateStr, String endDateStr, int workspaceId) throws IOException {
        String method = "GET";
        String url=String.format("%s/agm/api/reports/timesheet?login-names=%s&start-date=%s&end-date=%s&workspace-ids=%d", baseURL, userName, startDateStr, endDateStr, workspaceId);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + token.getAccessToken());

        RestResponse response = sendRequest(url, method, null, headers);
        try {
            List<TimesheetItem> items = parseTimesheetItems(response.getData());
            return items;
        }catch(Exception e) {
            logger.error("error in timesheet retrieve:", e);
            throw new AgmClientException("AGM_APP","ERROR_TIMESHEET_RETRIEVE", e.getMessage());
        }
    }

    public List<TimesheetItem> parseTimesheetItems(String json) throws JSONException, ParseException {
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("data");
        List<TimesheetItem> items = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject rawItem = (JSONObject) arr.get(i);
            TimesheetItem item = new TimesheetItem();
            items.add(item);
                    /* see reports/timesheet topic
                    for full list of fields. */
            int entityId = 0;
            try {
                entityId = Integer.parseInt(rawItem.getString("entityId"));
            } catch (Exception ignore) {
            }
            item.setEntityId(entityId);

            item.setEntityName(rawItem.getString("entityName"));
            item.setEntityType(rawItem.getString("entityType"));
            item.setLoginName(rawItem.getString("loginName"));
            item.setFullName(rawItem.getString("userFullName"));

            int investedHours = 0;
            try {
                investedHours = Integer.parseInt(rawItem.getString("investedHours"));
            } catch (Exception ignore) {
            }
            item.setInvested(investedHours);

            item.setDate(rawItem.getString("date")); //(format yyyy-MM-dd)

            int sprintId = 0;
            try {
                sprintId = Integer.parseInt(rawItem.getString("sprintId"));
            } catch (Exception ignore) {
            }
            item.setSprintId(sprintId);

            item.setSprintName(rawItem.getString("sprintName"));
        }
        return items;
    }



    /**
     * Creates the token needed for all REST requests, using form format
     * @param clientId the client id of the application wanting to access Agile Manager
     * @param clientSecret the secret of the client
     * @return an access token object holding the needed token information for all REST requests
     * @throws IOException
     * @throws JSONException
     */

    public AccessToken getAccessTokenWithFormFormat(String clientId, String clientSecret) throws IOException, JSONException {
        //http(s)://<server>:<port>/agm/oauth/token
        String url = String.format("%s/agm/oauth/token", baseURL);
        //client_id=<client_id>&client_secret=<client_secret>&grant_type=client_credentials
        String data = String.format("client_id=%s&client_secret=%s&grant_type=client_credentials", clientId, clientSecret);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        RestResponse response = sendRequest(url, HttpMethod.POST, data, headers);
        verifyResult(HttpStatus.SC_OK,response.getStatusCode());
        return AccessToken.createFromJson(response.getData());
    }


    public void setProxy(String host, int port){
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    /**
     * Sends a request to the server
     * @param url the url of the request
     * @param method the REST request method
     * @param data the data of the request
     * @param headers the headers of the request
     * @return the result of the request, as returned from the server
     * @throws IOException
     */

    private RestResponse sendRequest(String url, String method, String data, Map<String, String> headers) throws IOException {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = null;
            if (this.proxy != null) {
                con = (HttpURLConnection) obj.openConnection(this.proxy);
            } else {
                con = (HttpURLConnection) obj.openConnection();
            }

            con.setRequestMethod(method);

            //set headers

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            //set data

            if (data != null) {
                con.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
                wr.write(data);
                wr.flush();
                wr.close();
            }

            int responseCode = con.getResponseCode();
            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
            	in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {
            	InputStream inputStream = con.getErrorStream();
            	if (inputStream == null) {
            		inputStream = con.getInputStream();
            	}
            	in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));	
            }
            
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String output = response.toString();
            return new RestResponse(responseCode, output);
        }catch(IOException e) {
            logger.error("error in http connectivity:", e);
            throw new AgmClientException("AGM_APP","ERROR_HTTP_CONNECTIVITY_ERROR", e.getMessage());
        }
    }

    private void verifyResult(int expected, int result) {
        if(expected!=result)
        {
            logger.error("error in access token retrieve.");
            throw new AgmClientException("AGM_APP","ERROR_ACCESS_TOKEN_RETRIEVE");
        }
    }

    /**
     * Creates the headers for the REST requests
     * @param token the security token needed for every REST request
     * @return a map holding all the header parameters
     */

    private Map<String, String> createHeader(AccessToken token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + token.getAccessToken());
        headers.put("Content-Type", MediaType.APPLICATION_JSON + "; charset=UTF-8");
        return headers;
    }

}
