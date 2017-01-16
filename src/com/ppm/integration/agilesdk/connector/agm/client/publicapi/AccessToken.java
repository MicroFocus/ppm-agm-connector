package com.ppm.integration.agilesdk.connector.agm.client.publicapi;

import org.json.JSONException;
import org.json.JSONObject;




public class AccessToken {


    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private String scope;

    public AccessToken(String accessToken, String tokenType, int expiresIn, String scope) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public static AccessToken createFromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        return new AccessToken(obj.getString("access_token"), obj.getString("token_type"), obj.getInt("expires_in"), obj.getString("scope"));
    }

}
