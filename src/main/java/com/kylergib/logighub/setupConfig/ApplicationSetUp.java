package com.kylergib.logighub.setupConfig;

import com.kylergib.logighub.GHubClient;
import com.kylergib.logighub.LogiGHubPlugin;
import com.kylergib.logighub.device.Application;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;

public abstract class ApplicationSetUp {
    public static JSONObject appJSON;
    public static void getApps() {
        LogiGHubPlugin.LOGGER.log(Level.INFO, "Starting App Setup");
        JSONObject payload = (JSONObject) appJSON.get("payload");
        JSONArray apps = payload.getJSONArray("applications");
        apps.forEach(app -> {
            String applicationFolder = ((JSONObject) app).getString("applicationFolder");
            String name = ((JSONObject) app).getString("name");
            String applicationId = ((JSONObject) app).getString("applicationId");
            Application newApp = new Application(applicationId, applicationFolder, name);
            GHubClient.apps.add(newApp);
        });
    }
}
