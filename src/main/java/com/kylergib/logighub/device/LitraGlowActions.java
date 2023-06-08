package com.kylergib.logighub.device;

import com.kylergib.logighub.LogiGHubPlugin;
import com.kylergib.logighub.device.Application;
import com.kylergib.logighub.device.LitraGlow;
import com.kylergib.logighub.device.Profile;
import org.json.JSONObject;

import java.util.logging.Level;

public abstract class LitraGlowActions {
    public static String powerLitraDevice(LitraGlow litraGlow, Profile profile, boolean value) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("msgId", String.valueOf(LogiGHubPlugin.msgCount));
        LogiGHubPlugin.msgCount += 1;
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/card");

        JSONObject payload = new JSONObject();
        payload.put("id", profile.getILLUMINATION_LIGHT_POWER_SETTINGS());
        payload.put("name", "DEFAULT_CARD_NAME_ILLUMINATION_POWER_SETTINGS");
        payload.put("attribute", "ILLUMINATION_LIGHT_POWER_SETTINGS");
        payload.put("category", "");
        payload.put("readOnly", false);
        payload.put("applicationId", "");
        payload.put("profileId", profile.getId());
        payload.put("deviceSignature", litraGlow.getDeviceSignature());

        JSONObject illuminationLightPowerSettings = new JSONObject();
        illuminationLightPowerSettings.put("on", value);
        payload.put("illuminationLightPowerSettings", illuminationLightPowerSettings);

        jsonObject.put("payload", payload);
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.valueOf(jsonObject));
        return String.valueOf(jsonObject);

    }

    public static String switchActiveProfileForApp(Profile profile) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgId", String.valueOf(LogiGHubPlugin.msgCount));
        LogiGHubPlugin.msgCount += 1;
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/profile");

        JSONObject payload = new JSONObject();
        payload.put("id", profile.getId());
        payload.put("activeForApplication", true);

        jsonObject.put("payload", payload);
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.valueOf(jsonObject));
        return String.valueOf(jsonObject);
    }
    public static String setLEDColorForProfileWithPreset(LitraGlow litraGlow,
                                                         Profile profile, Application app,
                                                         int temp, int brightness) {
        //temp is 2700K (warmer) to 6500k (softer)
        //brightness is from 0% to 100%
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgId", String.valueOf(LogiGHubPlugin.msgCount));
        LogiGHubPlugin.msgCount += 1;
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/assignment");

        JSONObject payload = new JSONObject();
        payload.put("profile", app.getApplicationId());
        payload.put("slotId",litraGlow.getDeviceSignature()+":ILLUMINATION_LIGHT_COLOR_SETTINGS"); //ILLUMINATION_LIGHT.litra_glow.0.3167812477:ILLUMINATION_LIGHT_COLOR_SETTINGS


        JSONObject card = new JSONObject();
        card.put("id", litraGlow.getDEFAULT_CARD_ID());
        card.put("name", "DEFAULT_CARD_NAME_ILLUMINATION_COLOR_SETTINGS");
        card.put("attribute","ILLUMINATION_LIGHT_COLOR_SETTINGS");
        card.put("category","");
        card.put("readOnly", false);
        card.put("applicationId","");
        card.put("profileId","");
        card.put("deviceSignature",litraGlow.getDeviceSignature());

        JSONObject settings = new JSONObject();
        settings.put("temperature", temp);
        settings.put("brightness", brightness);
        settings.put("manualCard", true);

        JSONObject description = new JSONObject();
        description.put("slotPrefix","");

        settings.put("description", description);





        card.put("illuminationLightColorSettings",settings);
        payload.put("card", card);

        jsonObject.put("payload", payload);
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.valueOf(jsonObject));
        profile.setPresetActive(false);
        return String.valueOf(jsonObject);

    }

    public static String setLEDColorForProfileNoPreset(LitraGlow litraGlow,
                                     Profile profile, Application app,
                                     int temp, int brightness) {
        //temp is 2700K (warmer) to 6500k (softer)
        //brightness is from 1% to 100%
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgId", String.valueOf(LogiGHubPlugin.msgCount));
        LogiGHubPlugin.msgCount += 1;
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/card");

        JSONObject payload = new JSONObject();
        payload.put("id", litraGlow.getDEFAULT_CARD_ID());
        payload.put("name", "DEFAULT_CARD_NAME_ILLUMINATION_COLOR_SETTINGS");
        payload.put("attribute","ILLUMINATION_LIGHT_COLOR_SETTINGS");
        payload.put("category", "");
        payload.put("readOnly", false);
        payload.put("applicationId", "");
        payload.put("profileId", profile.getId());
        payload.put("deviceSignature", litraGlow.getDeviceSignature());

        JSONObject illuminationLightColorSettings = new JSONObject();
        illuminationLightColorSettings.put("temperature", temp);
        illuminationLightColorSettings.put("brightness", brightness);

        JSONObject description = new JSONObject();
        description.put("slotPrefix","");
        illuminationLightColorSettings.put("description", description);
        payload.put("manualCard", true);
        payload.put("illuminationLightColorSettings", illuminationLightColorSettings);

        jsonObject.put("payload", payload);
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.valueOf(jsonObject));
        return String.valueOf(jsonObject);

    }

    public static String enablePresetForProfile(LitraGlow litraGlow,
                                               Profile profile, Preset preset,
                                               Application app) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgId", String.valueOf(LogiGHubPlugin.msgCount));
        LogiGHubPlugin.msgCount += 1;
        jsonObject.put("verb", "SET");
        jsonObject.put("path", "/assignment");

        JSONObject payload = new JSONObject();
        payload.put("profile", profile.getId());
        payload.put("slotId",litraGlow.getDeviceSignature()+":ILLUMINATION_LIGHT_COLOR_SETTINGS"); //ILLUMINATION_LIGHT.litra_glow.0.3167812477:ILLUMINATION_LIGHT_COLOR_SETTINGS


        JSONObject card = new JSONObject();
        card.put("id", preset.getId());
        card.put("name", preset.getName());
        card.put("attribute","ILLUMINATION_LIGHT_COLOR_SETTINGS");
        card.put("category","");
        card.put("readOnly", true);
        card.put("applicationId","");
        card.put("profileId","");
        card.put("deviceSignature","ILLUMINATION_LIGHT.litra_glow");

        JSONObject settings = new JSONObject();
        settings.put("temperature", preset.getTemperature());
        settings.put("brightness", preset.getBrightness());
        settings.put("manualCard", false);

        JSONObject description = new JSONObject();
        description.put("slotPrefix","");

        settings.put("description", description);





        card.put("illuminationLightColorSettings",settings);
        payload.put("card", card);

        jsonObject.put("payload", payload);
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.valueOf(jsonObject));

        return jsonObject.toString();
    }


}
