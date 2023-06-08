package com.kylergib.logighub.device;

public class Profile {

    private String ILLUMINATION_LIGHT_COLOR_SETTINGS; //id
    private String ILLUMINATION_LIGHT_POWER_SETTINGS; //id
    private String ILLUMINATION_LIGHT_MEDIA_SETTINGS; //id
    private boolean activeForApplication;
    private String name;
    private String id;
    private String applicationId;
    private boolean hasPersistentAssignments;
    private String deviceSignature;
    private boolean poweredOn;
    private int brightness; //stored for percent from 0-100
    private int temperature; //stored between 2700-6500 to nearest hundred
    private boolean presetActive;


    public Profile(String ILLUMINATION_LIGHT_COLOR_SETTINGS, String ILLUMINATION_LIGHT_POWER_SETTINGS,
                   String ILLUMINATION_LIGHT_MEDIA_SETTINGS, boolean activeForApplication, String name,
                   String id, String applicationId, boolean hasPersistentAssignments,
                   String deviceSignature) {
        this.ILLUMINATION_LIGHT_COLOR_SETTINGS = ILLUMINATION_LIGHT_COLOR_SETTINGS;
        this.ILLUMINATION_LIGHT_POWER_SETTINGS = ILLUMINATION_LIGHT_POWER_SETTINGS;
        this.ILLUMINATION_LIGHT_MEDIA_SETTINGS = ILLUMINATION_LIGHT_MEDIA_SETTINGS;
        this.activeForApplication = activeForApplication;
        this.name = name;
        this.id = id;
        this.applicationId = applicationId;
        this.hasPersistentAssignments = hasPersistentAssignments;
        this.deviceSignature = deviceSignature;
    }

    public String getILLUMINATION_LIGHT_COLOR_SETTINGS() {
        return ILLUMINATION_LIGHT_COLOR_SETTINGS;
    }

    public void setILLUMINATION_LIGHT_COLOR_SETTINGS(String ILLUMINATION_LIGHT_COLOR_SETTINGS) {
        this.ILLUMINATION_LIGHT_COLOR_SETTINGS = ILLUMINATION_LIGHT_COLOR_SETTINGS;
    }

    public String getILLUMINATION_LIGHT_POWER_SETTINGS() {
        return ILLUMINATION_LIGHT_POWER_SETTINGS;
    }

    public void setILLUMINATION_LIGHT_POWER_SETTINGS(String ILLUMINATION_LIGHT_POWER_SETTINGS) {
        this.ILLUMINATION_LIGHT_POWER_SETTINGS = ILLUMINATION_LIGHT_POWER_SETTINGS;
    }

    public String getILLUMINATION_LIGHT_MEDIA_SETTINGS() {
        return ILLUMINATION_LIGHT_MEDIA_SETTINGS;
    }

    public void setILLUMINATION_LIGHT_MEDIA_SETTINGS(String ILLUMINATION_LIGHT_MEDIA_SETTINGS) {
        this.ILLUMINATION_LIGHT_MEDIA_SETTINGS = ILLUMINATION_LIGHT_MEDIA_SETTINGS;
    }

    public boolean isActiveForApplication() {
        return activeForApplication;
    }

    public void setActiveForApplication(boolean activeForApplication) {
        this.activeForApplication = activeForApplication;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isHasPersistentAssignments() {
        return hasPersistentAssignments;
    }

    public void setHasPersistentAssignments(boolean hasPersistentAssignments) {
        this.hasPersistentAssignments = hasPersistentAssignments;
    }

    public String getDeviceSignature() {
        return deviceSignature;
    }

    public void setDeviceSignature(String deviceSignature) {
        this.deviceSignature = deviceSignature;
    }

    public boolean isPoweredOn() {
        return poweredOn;
    }

    public void setPoweredOn(boolean poweredOn) {
        this.poweredOn = poweredOn;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;

    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public boolean isPresetActive() {
        return presetActive;
    }

    public void setPresetActive(boolean presetActive) {
        this.presetActive = presetActive;
    }
}
