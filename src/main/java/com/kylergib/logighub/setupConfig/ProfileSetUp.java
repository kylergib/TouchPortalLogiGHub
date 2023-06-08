package com.kylergib.logighub.setupConfig;

import com.kylergib.logighub.GHubClient;
import com.kylergib.logighub.LogiGHubPlugin;
import com.kylergib.logighub.device.LitraGlow;
import com.kylergib.logighub.device.Preset;
import com.kylergib.logighub.device.Profile;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.kylergib.logighub.GHubClient.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ProfileSetUp {
    //TODO: clear all jsons/lists when reconnecting to ghub
    public static JSONObject allProfileJSON;
    public static JSONObject cardJSON;
    public static JSONObject activeProfileJSON;


    //msg id is 52
    public static void sortProfiles() {
        LogiGHubPlugin.LOGGER.log(Level.FINE, "Sorting Profiles");
        JSONObject payload = (JSONObject) allProfileJSON.get("payload");
        JSONArray profiles = (JSONArray) payload.get("profiles");
        profiles.forEach(profile -> {
            String ILLUMINATION_LIGHT_COLOR_SETTINGS = null;
            String ILLUMINATION_LIGHT_POWER_SETTINGS = null;
            String ILLUMINATION_LIGHT_MEDIA_SETTINGS = null;
            boolean activeForApplication;
            String name;
            String id;
            String applicationId;
            boolean hasPersistentAssignments;
            String deviceSignature = null;

            JSONArray assignments = (JSONArray) ((JSONObject) profile).get("assignments");
            for (int i = 0; i < assignments.length(); i++) {
                JSONObject card = (JSONObject) assignments.get(i);
                String cardId = (String) card.get("cardId");
                String slotId = (String) card.get("slotId");
                deviceSignature = slotId.split(":")[0];
                String settingName = slotId.split(":")[1];
                //TODO: dont think this is needed now
                switch (settingName) {
                    case "ILLUMINATION_LIGHT_COLOR_SETTINGS":
                        ILLUMINATION_LIGHT_COLOR_SETTINGS = cardId;
                        break;
                    case "ILLUMINATION_LIGHT_POWER_SETTINGS":
                        ILLUMINATION_LIGHT_POWER_SETTINGS = cardId;
                        break;
                    case "ILLUMINATION_LIGHT_MEDIA_SETTINGS":
                        ILLUMINATION_LIGHT_MEDIA_SETTINGS = cardId;
                        break;
                }
            }
            activeForApplication = ((JSONObject) profile).getBoolean("activeForApplication");
            name = ((JSONObject) profile).getString("name");
            id = ((JSONObject) profile).getString("id");
            applicationId = ((JSONObject) profile).getString("applicationId");
            hasPersistentAssignments = ((JSONObject) profile).getBoolean("hasPersistentAssignments");
            Profile newProfile = new Profile(ILLUMINATION_LIGHT_COLOR_SETTINGS, ILLUMINATION_LIGHT_POWER_SETTINGS,
                    ILLUMINATION_LIGHT_MEDIA_SETTINGS, activeForApplication, name, id,
                    applicationId, hasPersistentAssignments, deviceSignature);
            devices.get(0).addProfile(newProfile);
            //TODO: add to own list to make sure? i think it is system wide?
            GHubClient.profiles.add(newProfile);

        });
    }

    //msg id is 56, sortProfiles() needs to run first
    public static void getProfileSettings() {
        LogiGHubPlugin.LOGGER.log(Level.FINE, "Getting Profile Settings");
        List<String> presetStrings = new ArrayList<>();
        List<String> defaultCardStrings = new ArrayList<>();
        JSONObject payload = cardJSON.getJSONObject("payload");
        JSONArray cards = payload.getJSONArray("cards");
        int count = 0;
        for (int i = 0; i < cards.length(); i++) {
            count += 1;
            JSONObject card =  (JSONObject) cards.get(i);

            String name = ((JSONObject) card).getString("name");

            String attribute = card.getString("attribute");

            String deviceSignature = ((JSONObject) card).getString("deviceSignature");

            String cardId = card.getString("id");
            List<Profile> foundProfiles = GHubClient.profiles.stream().filter(profile -> {
                switch (attribute) {
                    case "ILLUMINATION_LIGHT_POWER_SETTINGS":
                        return profile.getILLUMINATION_LIGHT_POWER_SETTINGS().equals(cardId);
                    case "ILLUMINATION_LIGHT_COLOR_SETTINGS":
                        return profile.getILLUMINATION_LIGHT_COLOR_SETTINGS().equals(cardId);
                    default:
                        return false;
                }

            }).collect(Collectors.toList());
            Profile currentProfile = null;
            if (foundProfiles.size() > 0) {
                currentProfile = foundProfiles.get(0);
            } else if (card.keySet().contains("macro")) {
                JSONObject macro = card.getJSONObject("macro");
                String type = macro.getString("type");
                if (macro.keySet().contains("linkedCard")) {
                    String linkedCard = macro.getString("linkedCard");
                    if (type.equals("ILLUMINATION_LIGHT_PRESET") && !name.contains("DEFAULT_CARD")) {
                        if (!presetStrings.contains(linkedCard)) presetStrings.add(linkedCard);
                    }
                    else if (type.equals("ILLUMINATION_LIGHT_PRESET") && name.contains("DEFAULT_CARD")) {
                        if (!defaultCardStrings.contains(linkedCard)) defaultCardStrings.add(linkedCard);
                    }
                }

            }







            if (attribute.equals("ILLUMINATION_LIGHT_MEDIA_SETTINGS")){
                //TODO: i dont use this function but someone may find it necessary
            } else if (attribute.equals("ILLUMINATION_LIGHT_POWER_SETTINGS")) {
                JSONObject settings = card.getJSONObject("illuminationLightPowerSettings");
                boolean poweredOn = settings.getBoolean("on");
                if (currentProfile != null) {
                    currentProfile.setPoweredOn(poweredOn);
                    LogiGHubPlugin.LOGGER.log(Level.FINE,String.format("%s:%s poweredOn: %b",
                            currentProfile.getName(),currentProfile.getApplicationId(),
                            currentProfile.isPoweredOn()));
                }

            } else if (attribute.equals("ILLUMINATION_LIGHT_COLOR_SETTINGS")) {

                JSONObject settings = card.getJSONObject("illuminationLightColorSettings");
                int brightness = settings.getInt("brightness");
                int temperature = settings.getInt("temperature");
                if (currentProfile != null) {
                    currentProfile.setBrightness(brightness);
                    currentProfile.setTemperature(temperature);
                    LogiGHubPlugin.LOGGER.log(Level.FINE,String.format("%s:%s brightness: %d, temp: %d",
                            currentProfile.getName(),currentProfile.getApplicationId(),
                            currentProfile.getBrightness(),currentProfile.getTemperature()));
                }
                if (presetStrings.contains(cardId)) {

                    String presetName;
                    if (name.contains("ILLUMINATION_LIGHT_")) presetName = name.replace("ILLUMINATION_LIGHT_","");
                    else presetName = name;
                    Preset newPreset = new Preset(cardId,name,presetName,brightness,temperature);
                    GHubClient.presets.add(newPreset);
                } else if (defaultCardStrings.contains(cardId)) {
                    List<LitraGlow> foundDevice = devices.stream().filter(device -> device.getDeviceSignature().equals(deviceSignature)).collect(Collectors.toList());
                    if (foundDevice.size() == 1) {
                        foundDevice.get(0).setDEFAULT_CARD_ID(cardId);
                    }
                }


            }

        }
        LogiGHubPlugin.LOGGER.log(Level.INFO, String.format("Sorted %d of %d possible cards", count,cards.length()));

    }
    //msg id is 58
    public static void getActiveProfile() {
        JSONObject payload = (JSONObject) activeProfileJSON.get("payload");
        String activeId = payload.getString("id");
        List<Profile> profiles = GHubClient.profiles.stream().filter(profile ->
            profile.getId().equals(activeId)).collect(Collectors.toList());
        if (profiles.size() > 0) GHubClient.activeProfile = profiles.get(0);
        //TODO: active profile is not right since every app has an active profile?


    }
}
