package com.ppm.integration.agilesdk.connector.agm.client.publicapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;




public class ReleaseEntity {

    private int id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private int sprintDuration;
    private SprintDurationUnitsEnum sprintDurationUnits;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getSprintDuration() {
        return sprintDuration;
    }

    public void setSprintDuration(int sprintDuration) {
        this.sprintDuration = sprintDuration;
    }

    public SprintDurationUnitsEnum getSprintDurationUnits() {
        return sprintDurationUnits;
    }

    public void setSprintDurationUnits(SprintDurationUnitsEnum sprintDurationUnits) {
        this.sprintDurationUnits = sprintDurationUnits;
    }


    public String generateBody(){
        StringBuffer sb=new StringBuffer();
        sb.append("{ \"data\":[{");
        sb.append(String.format("\"name\": \"%s\", ",getName()));
        sb.append(String.format("\"description\": \"%s\", ",getDescription()));
        sb.append(String.format("\"start_date\": \"%s\", ",getStartDate()));
        sb.append(String.format("\"end_date\": \"%s\", ",getEndDate()));
        sb.append(String.format("\"sprint_duration\": \"%d\", ",getSprintDuration()));
        sb.append(String.format("\"sprint_duration_units\" : \"%s\" ",getSprintDurationUnits().getText()));
        sb.append("}]}");
        return sb.toString();
    }

    public static ReleaseEntity createFromJson(String json) throws JSONException, ParseException {
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("data");
        JSONObject item= (JSONObject) arr.get(0);
        ReleaseEntity result=new ReleaseEntity();
        result.setId(item.getInt("id"));
        result.setName(item.getString("name"));
        result.setDescription(item.getString("description"));
        result.setStartDate(item.getString("start_date"));
        result.setEndDate(item.getString("end_date"));
        result.setSprintDuration(item.getInt("sprint_duration"));
        String sprintDurationUnits = item.getString("sprint_duration_units");
        result.setSprintDurationUnits(SprintDurationUnitsEnum.fromText(sprintDurationUnits));
        return result;
    }
}
