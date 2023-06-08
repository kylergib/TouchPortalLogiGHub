package com.kylergib.logighub.device;

import java.util.ArrayList;
import java.util.List;

public class LitraGlow {
    private String extendedDisplayName;
    private String givenName;
    private int pid;
    private String deviceSignature;
    private String state;
    private boolean resourcesAvailable;
    private String deviceType;
    private boolean individualMode;
    private String deviceBaseModel;
    private String deviceUnitId;
    private String deviceModel;
    private static String ILLUMINATION_LIGHT_MEDIA_SETTINGS;
    private static String ILLUMINATION_LIGHT_COLOR_SETTINGS;
    private static String ILLUMINATION_LIGHT_POWER_SETTINGS;
    private String DEFAULT_CARD_ID;
    private List<Profile> profileList;



    public LitraGlow(String extendedDisplayName, String givenName,
                     int pid, String deviceSignature, String state,
                     boolean resourcesAvailable, String deviceType,
                     boolean individualMode, String deviceBaseModel,
                     String deviceUnitId, String deviceModel) {
        this.extendedDisplayName = extendedDisplayName;
        this.givenName = givenName;
        this.pid = pid;
        this.deviceSignature = deviceSignature;
        this.state = state;
        this.resourcesAvailable = resourcesAvailable;
        this.deviceType = deviceType;
        this.individualMode = individualMode;
        this.deviceBaseModel = deviceBaseModel;
        this.deviceUnitId = deviceUnitId;
        this.deviceModel = deviceModel;
        profileList = new ArrayList<>();
    }

    public static String getIlluminationLightPowerSettings() {
        return ILLUMINATION_LIGHT_POWER_SETTINGS;
    }

    public static void setIlluminationLightPowerSettings(String illuminationLightPowerSettings) {
        ILLUMINATION_LIGHT_POWER_SETTINGS = illuminationLightPowerSettings;
    }

    public static String getIlluminationLightColorSettings() {
        return ILLUMINATION_LIGHT_COLOR_SETTINGS;
    }

    public static void setIlluminationLightColorSettings(String illuminationLightColorSettings) {
        ILLUMINATION_LIGHT_COLOR_SETTINGS = illuminationLightColorSettings;
    }

    public static String getIlluminationLightMediaSettings() {
        return ILLUMINATION_LIGHT_MEDIA_SETTINGS;
    }

    public static void setIlluminationLightMediaSettings(String illuminationLightMediaSettings) {
        ILLUMINATION_LIGHT_MEDIA_SETTINGS = illuminationLightMediaSettings;
    }

    public String getExtendedDisplayName() {
        return extendedDisplayName;
    }

    public void setExtendedDisplayName(String extendedDisplayName) {
        this.extendedDisplayName = extendedDisplayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getDeviceSignature() {
        return deviceSignature;
    }

    public void setDeviceSignature(String deviceSignature) {
        this.deviceSignature = deviceSignature;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isResourcesAvailable() {
        return resourcesAvailable;
    }

    public void setResourcesAvailable(boolean resourcesAvailable) {
        this.resourcesAvailable = resourcesAvailable;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isIndividualMode() {
        return individualMode;
    }

    public void setIndividualMode(boolean individualMode) {
        this.individualMode = individualMode;
    }

    public String getDeviceBaseModel() {
        return deviceBaseModel;
    }

    public void setDeviceBaseModel(String deviceBaseModel) {
        this.deviceBaseModel = deviceBaseModel;
    }

    public String getDeviceUnitId() {
        return deviceUnitId;
    }

    public void setDeviceUnitId(String deviceUnitId) {
        this.deviceUnitId = deviceUnitId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public List<Profile> getProfileList() {
        return profileList;
    }

    public void addProfile(Profile profile) {
        this.profileList.add(profile);
    }

    public String getDEFAULT_CARD_ID() {
        return DEFAULT_CARD_ID;
    }

    public void setDEFAULT_CARD_ID(String DEFAULT_CARD_ID) {
        this.DEFAULT_CARD_ID = DEFAULT_CARD_ID;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }
}
