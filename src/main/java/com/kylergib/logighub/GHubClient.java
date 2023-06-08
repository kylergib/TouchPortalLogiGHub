package com.kylergib.logighub;


import com.kylergib.logighub.device.Application;
import com.kylergib.logighub.device.LitraGlow;
import com.kylergib.logighub.device.Preset;
import com.kylergib.logighub.device.Profile;
import com.kylergib.logighub.setupConfig.ApplicationSetUp;
import com.kylergib.logighub.setupConfig.DeviceSetup;
import com.kylergib.logighub.setupConfig.ProfileSetUp;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GHubClient extends WebSocketClient {

    public boolean isConnected;
    public static List<LitraGlow> devices;
    public static List<Profile> profiles;
    public static List<Application> apps;
    public static List<Preset> presets;

    public static Profile activeProfile;
    private int initialConfigs;
    private boolean needConfigs;
    private int deviceAndProfileConfigs;
    private final ConfigCallback backendCallback;




    public GHubClient(String host, int port,ConfigCallback backendCallback) throws URISyntaxException {
        super(new URI("ws://" + host + ":" + port));

        addHeader("Origin", "file://");
        addHeader("Pragma", "no-cache");
        addHeader("Cache-Control", "no-cache");
        addHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        addHeader("Sec-WebSocket-Protocol", "json");

        connect();
        this.backendCallback = backendCallback;

        initialConfigs = 0;
        needConfigs = true;
        deviceAndProfileConfigs = 0;

    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        isConnected = true;
        LogiGHubPlugin.latch.countDown();
        LogiGHubPlugin.LOGGER.log(Level.INFO, "Connected to GHub");
    }

    @Override
    public void onMessage(String message) {
        JSONObject messageJson = new JSONObject(message);
        LogiGHubPlugin.LOGGER.log(Level.FINER,"1");
        String messageId = messageJson.getString("msgId");
        String verb = messageJson.getString("verb");
        String path = messageJson.getString("path");
        LogiGHubPlugin.LOGGER.log(Level.FINER,"2");

        String origin = messageJson.getString("origin");

        //creates a folder and adds files ending in .json to see what is sent from ghub app
        if (LogiGHubPlugin.debugSetting > 2) {
            String folderPath = "json/";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    LogiGHubPlugin.LOGGER.log(Level.WARNING, "Failed to create the folder.");
                    return;
                } else LogiGHubPlugin.LOGGER.log(Level.INFO,"Created folder: " + folder.getAbsolutePath());

            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(messageJson);
            if (messageId != null && !messageId.isEmpty()) {
                try (FileWriter fileWriter = new FileWriter(folderPath + File.separator + messageId + "-output.json")) {
                    fileWriter.write(String.valueOf(jsonString));
                    LogiGHubPlugin.LOGGER.log(Level.FINER,"created new file: " + messageId + "-output.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        switch (messageId) {
            case "50":
                if (checkResult(messageJson)) {
                    DeviceSetup.deviceListJSON = messageJson;
                    deviceAndProfileConfigs += 1;
                }
                break;
            case "52":
                if (checkResult(messageJson)) {

                    ProfileSetUp.allProfileJSON = messageJson;
                    deviceAndProfileConfigs += 1;
                }
                break;
            case "56":
                if (checkResult(messageJson)) {
                    ProfileSetUp.cardJSON = messageJson;
                    deviceAndProfileConfigs += 1;
                }
                break;
            case "57":
                if (checkResult(messageJson)) {
                    ApplicationSetUp.appJSON = messageJson;
                    deviceAndProfileConfigs += 1;
                }
                break;
            case "58":
                if (checkResult(messageJson)) {
                    ProfileSetUp.activeProfileJSON = messageJson;
                    deviceAndProfileConfigs += 1;
                }
                break;
        }
        if (deviceAndProfileConfigs == 5 && LogiGHubPlugin.needDevices) {
            backendCallback.onProfileAndDeviceReceived();
        }
        LogiGHubPlugin.LOGGER.log(Level.FINER,"5");

        if (origin.equals("backend") && verb.equals("OPTIONS")) {
            initialConfigs += 1;

        } else if (verb.equals("GET") && path.equals("/feature_flags")) {
            initialConfigs += 1;
        } else if (verb.equals("GET") && path.equals("/crash_reporting/status")) {
            initialConfigs += 1;
        } else if (verb.equals("GET") && path.equals("/community/lumen/environment")) {
            initialConfigs += 1;
        } else if (verb.equals("GET") && path.equals("/updates/pipeline/info")) {
            initialConfigs += 1;
        }
        LogiGHubPlugin.LOGGER.log(Level.FINER,"6");
        if (initialConfigs == 5 && needConfigs) {
            needConfigs = false;
            deviceAndProfileConfigs = 0;
            backendCallback.onBackendReceived();

        }
        LogiGHubPlugin.LOGGER.log(Level.FINER,"7");
        if (path.equals("/devices/state/changed") && LogiGHubPlugin.needDevices) {
            deviceAndProfileConfigs = 0;
            backendCallback.onBackendReceived();
        }
        if (verb.equals("BROADCAST")) {

            JSONObject payload = messageJson.getJSONObject("payload");
            if (path.equals("/cards/updated")) {

                JSONArray updatedCards = payload.getJSONArray("updatedCards");
                updatedCards.forEach(card -> {
                    String name = ((JSONObject) card).getString("name");
                    switch (name) {
                        case "DEFAULT_CARD_NAME_ILLUMINATION_POWER_SETTINGS":
                            illuminationLightPowerSettingsUpdated((JSONObject) card);
                            break;
                        case "DEFAULT_CARD_NAME_ILLUMINATION_COLOR_SETTINGS":
                            illuminationLightColorSettingsUpdated((JSONObject) card);
                            break;

                    }
                });
            }

        }
        LogiGHubPlugin.LOGGER.log(Level.FINER,"8");

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
    }

    @Override
    public void onError(Exception ex) {
        isConnected = false;
    }

    public boolean checkResult(JSONObject json) {
        JSONObject result = (JSONObject) json.get("result");
        String code = (String) result.get("code");

        return code.equals("SUCCESS");
    }
    public void illuminationLightPowerSettingsUpdated(JSONObject card) {

//        String deviceSignature = card.getString("deviceSignature");
        String id = card.getString("id");
        JSONObject settings = card.getJSONObject("illuminationLightPowerSettings");
        boolean isPoweredOn = settings.getBoolean("on");

        //look for profile
        List<Profile> filteredProfiles = profiles.stream().filter(profile -> profile.getILLUMINATION_LIGHT_POWER_SETTINGS().equals(id)).collect(Collectors.toList());
        if (filteredProfiles.size() == 1) {
            Profile selectedProfile = filteredProfiles.get(0);
            selectedProfile.setPoweredOn(isPoweredOn);
            }

    }
    public void illuminationLightColorSettingsUpdated(JSONObject card) {
        String deviceSignature = card.getString("deviceSignature");
        String id = card.getString("id");
        JSONObject settings = card.getJSONObject("illuminationLightColorSettings");
        int brightness = settings.getInt("brightness");
        int convertedBrightness = Math.toIntExact(Math.round(LogiGHubPlugin.convertBrightnessToPercentage(brightness)));
        int temp = settings.getInt("temperature");
        int sliderTemp = Math.toIntExact(Math.round(LogiGHubPlugin.convertTemperatureToPercentage(temp)));
        List<Profile> filteredProfiles = profiles.stream().filter(profile -> profile.getILLUMINATION_LIGHT_COLOR_SETTINGS().equals(id)).collect(Collectors.toList());
        if (filteredProfiles.size() == 1) {
            Profile selectedProfile = filteredProfiles.get(0);

            LitraGlow device = LogiGHubPlugin.findDeviceBySignature(deviceSignature);
            Application app = LogiGHubPlugin.findAppById(selectedProfile.getApplicationId());
            HashMap<String, Object> brightnessData = new HashMap<>();
            HashMap<String, Object> temperatureData = new HashMap<>();

            if (device != null && app != null) {
                if (selectedProfile.getBrightness() != convertedBrightness) {
                    selectedProfile.setBrightness(convertedBrightness);

                    brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileBrightnessConnector.data.profiles", selectedProfile.getName());
                    brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileBrightnessConnector.data.apps", app.getApplicationName());
                    brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.devices", device.getGivenName());
                    LogiGHubPlugin.logiGHubPlugin.sendConnectorUpdate(LogiGHubPluginConstants.ID,
                            LogiGHubPluginConstants.LogiGHub.Connectors.ProfileBrightnessConnector.ID, convertedBrightness, brightnessData);
                }
                if (selectedProfile.getTemperature() != temp) {
                    selectedProfile.setTemperature(temp);
                    temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileTemperatureConnector.data.profiles", selectedProfile.getName());
                    temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileTemperatureConnector.data.apps", app.getApplicationName());
                    temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.devices", device.getGivenName());

                    LogiGHubPlugin.logiGHubPlugin.sendConnectorUpdate(LogiGHubPluginConstants.ID,
                            LogiGHubPluginConstants.LogiGHub.Connectors.ProfileTemperatureConnector.ID, sliderTemp, temperatureData);
                }
            }

        }


    }
}