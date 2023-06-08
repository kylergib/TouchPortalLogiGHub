package com.kylergib.logighub;

import com.christophecvb.touchportal.annotations.State;

public abstract class TPStates {

    @State(valueChoices = {}, defaultValue = "", categoryId = "LogiGHub")
    public static String[] devices;

    @State(valueChoices = {}, defaultValue = "", categoryId = "LogiGHub")
    public static String[] profiles;

    @State(defaultValue = "", categoryId = "LogiGHub")
    public static String[] presets;

    @State(defaultValue = "", categoryId = "LogiGHub")
    public static String[] activeProfile;

}
