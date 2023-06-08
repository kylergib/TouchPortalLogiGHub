package com.kylergib.logighub.device;

public class Preset {
    private String id;
    private String name;
    private String presetName;
    private int brightness;
    private int temperature;

    public Preset(String id, String name, String presetName, int brightness, int temperature) {
        this.id = id;
        this.name = name;
        this.presetName = presetName;
        this.brightness = brightness;
        this.temperature = temperature;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
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
}
