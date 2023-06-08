package com.kylergib.logighub;

import org.json.JSONObject;

public abstract class Setup {
    public static String pathAndVerbJson(String msgId,String verbString, String pathString) {

        JSONObject jsonObject = new JSONObject();
        if (msgId != null)  jsonObject.put("msgId",msgId);
        jsonObject.put("path",pathString);
        jsonObject.put("verb",verbString);
        LogiGHubPlugin.msgCount += 1;
        return String.valueOf(jsonObject);
    }
    public static String pathAndVerbJson(String msgId,String verbString, String pathString, boolean needParams) {
        JSONObject jsonObject = new JSONObject();
        if (msgId != null)  jsonObject.put("msgId",msgId);
        jsonObject.put("path",pathString);
        jsonObject.put("verb",verbString);
        if (needParams) {
            JSONObject params = new JSONObject();
            jsonObject.put("payload", params);
        }
        return String.valueOf(jsonObject);
    }
    public static String getLocalPackages() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("msg_id", "1");
        jsonObject.put("verb", "GET");
        jsonObject.put("path", "/resources/localized_packages");

        JSONObject payload = new JSONObject();
        payload.put("packages", "core/data/strings/common");

        jsonObject.put("payload", payload);
        return String.valueOf(jsonObject);
    }
    public static String setCommunityUser() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("msg_id", "2");
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/community/user/me");

        JSONObject payload = new JSONObject();

        jsonObject.put("payload", payload);
        return String.valueOf(jsonObject);
    }
}
