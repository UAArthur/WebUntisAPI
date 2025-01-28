package de.keule.webuntis.response;

import org.json.JSONObject;

public class TokenData {
    private JSONObject json;
    private String token;

    public TokenData(JSONObject json) {
        this.json = json;
        this.token = json.getJSONObject("result").getString("token");
    }

    public JSONObject getJson() {
        return json;
    }

    public String getToken() {
        return token;
    }
}
