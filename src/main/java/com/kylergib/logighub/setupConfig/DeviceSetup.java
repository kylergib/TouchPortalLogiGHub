package com.kylergib.logighub.setupConfig;

import com.kylergib.logighub.GHubLogger;
import com.kylergib.logighub.LogiGHubPlugin;
import com.kylergib.logighub.device.LitraGlow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;

import static com.kylergib.logighub.GHubClient.devices;

public abstract class DeviceSetup {
    public static JSONObject deviceListJSON;


    public static boolean createDevices() {
        GHubLogger.gLogger.addMessage("createDevices init");
        LogiGHubPlugin.LOGGER.log(Level.FINE, "Starting create devices");
        JSONObject payload = (JSONObject) deviceListJSON.get("payload");
        if (!payload.keySet().contains("deviceInfos")) {
            LogiGHubPlugin.LOGGER.log(Level.FINE, "No devices found");
            GHubLogger.gLogger.addMessage("no devices found");
            LogiGHubPlugin.needDevices = true;

        }
        LogiGHubPlugin.LOGGER.log(Level.INFO, "Devices were found");
        GHubLogger.gLogger.addMessage("devices found");
        JSONArray devicesInfos = (JSONArray) payload.get("deviceInfos");
        devicesInfos.forEach(myArrayList -> {



            String extendedDisplayName = (String) ((JSONObject) myArrayList).get("extendedDisplayName");
            String displayName = (String) ((JSONObject) myArrayList).get("displayName");
            String givenName = (String) ((JSONObject) myArrayList).get("givenName");
            int pid = ((JSONObject) myArrayList).getInt("pid");
            String deviceSignature = (String) ((JSONObject) myArrayList).get("deviceSignature");
            String state = (String) ((JSONObject) myArrayList).get("state");
            boolean resourcesAvailable = ((JSONObject) myArrayList).getBoolean("resourcesAvailable");
            String deviceType = (String) ((JSONObject) myArrayList).get("deviceType");
            boolean individualMode = (boolean) ((JSONObject) myArrayList).get("individualMode");
            String deviceBaseModel = (String) ((JSONObject) myArrayList).get("deviceBaseModel");
            String deviceUnitId = (String) ((JSONObject) myArrayList).get("deviceUnitId");
            String deviceModel = (String) ((JSONObject) myArrayList).get("deviceModel");
            String result = "extendedDisplayName: " + extendedDisplayName +
                    ", displayName: " + displayName +
                    ", givenName: " + givenName +
                    ", pid: " + pid +
                    ", deviceSignature: " + deviceSignature +
                    ", state: " + state +
                    ", resourcesAvailable: " + resourcesAvailable +
                    ", deviceType: " + deviceType +
                    ", individualMode: " + individualMode +
                    ", deviceBaseModel: " + deviceBaseModel +
                    ", deviceUnitId: " + deviceUnitId +
                    ", deviceModel: " + deviceModel;
            GHubLogger.gLogger.addMessage(extendedDisplayName);


            if (!givenName.equals(""))  displayName = givenName;
            if (deviceType.equals("ILLUMINATION_LIGHT")) {
                GHubLogger.gLogger.addMessage(result);
                LitraGlow litraGlow = new LitraGlow(extendedDisplayName,displayName,
                        pid,deviceSignature,state,resourcesAvailable,deviceType,
                        individualMode,deviceBaseModel,deviceUnitId,deviceModel);
                devices.add(litraGlow);
            }
        });
        GHubLogger.gLogger.addMessage("num devices" + String.valueOf(devices.size()));
        GHubLogger.gLogger.addMessage("need devices is: " + String.valueOf(devices.size() > 0));

        if (devices.size() > 0) LogiGHubPlugin.needDevices = false;

        return !LogiGHubPlugin.needDevices;

    }
}
