package com.kylergib.logighub;

import com.google.gson.JsonObject;
import com.kylergib.logighub.device.*;
import com.kylergib.logighub.setupConfig.ApplicationSetUp;
import com.kylergib.logighub.setupConfig.DeviceSetup;
import com.kylergib.logighub.setupConfig.ProfileSetUp;

import com.christophecvb.touchportal.TouchPortalPlugin;
import com.christophecvb.touchportal.annotations.*;
import com.christophecvb.touchportal.helpers.PluginHelper;
import com.christophecvb.touchportal.model.*;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//TODO: see if i can upate connectors on start up
@Plugin(version = BuildConfig.VERSION_CODE, colorDark = "#203060", colorLight = "#4070F0", name = "Logi GHub Plugin")
public class LogiGHubPlugin extends TouchPortalPlugin implements TouchPortalPlugin.TouchPortalPluginListener,
        ConfigCallback, MonitorAppThread.AppOpenCallback {
    public static CountDownLatch latch;
    public static LogiGHubPlugin logiGHubPlugin;
    public final static Logger LOGGER = Logger.getLogger(TouchPortalPlugin.class.getName());
    public static String currentIp;
    public static GHubClient gHubClient;
    public static boolean needDevices;
    public static boolean retrySent;
    public static int msgCount;
    private static boolean appIsOpen;
    private MonitorAppThread monitorAppThread;

    public final static Level INFO = Level.INFO;
    public final static Level FINE = Level.FINE;
    public final static Level FINER = Level.FINER;
    public final static Level FINEST = Level.FINEST;
    public final static Level WARNING = Level.WARNING;
    public final static Level SEVERE = Level.SEVERE;

    @Override
    public void onAppOpened() {
        LOGGER.log(INFO, "G Hub opened");
        appIsOpen = true;
        try {
            connectToGHub();
        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAppClosed() {
        LOGGER.log(WARNING, "G Hub closed");
        appIsOpen = false;
        gHubClient.close();
    }


    private enum Categories {
        /**
         * Category definitions
         */
        //TODO: replace imagepath
        @Category(name = "Logi G Hub", imagePath = "")
        LogiGHub
    }



    /**
     * Debug setting in touch portal
     */
    @Setting(name = "Debug", defaultValue = "1", maxLength = 15)
    public static int debugSetting;


    /**
     * Constructor calling super
     */
    public LogiGHubPlugin() {
        super(true);// true is for paralleling Actions executions
    }

    public static void main(String... args) {

        if (args != null && args.length == 1) {
            if (PluginHelper.COMMAND_START.equals(args[0])) {
                // Initialize the Plugin
                appIsOpen = false;

                logiGHubPlugin = new LogiGHubPlugin();
                logiGHubPlugin.connectThenPairAndListen(logiGHubPlugin);

            }
        }
    }







    @Override
    public void onDisconnected(Exception exception) {
        LOGGER.log(Level.INFO, "Disconnected");
        monitorAppThread.requestStop();
        System.exit(0);

    }

    @Override
    public void onReceived(JsonObject jsonMessage) {
    }

    @Override
    public void onInfo(TPInfoMessage tpInfoMessage) {
        boolean updateAvailable = checkForUpdate();
        if (updateAvailable) {
            logiGHubPlugin.sendShowNotification(
                    LogiGHubPluginConstants.LogiGHub.ID + ".updateNotification",
                    "Update is available. ",
                    "You are on version: " + BuildConfig.VERSION_CODE + " and update is available on GitHub",
                    new TPNotificationOption[]{
                            new TPNotificationOption(LogiGHubPluginConstants.LogiGHub.ID + ".updateNotification.options.openLink", "Open Link")
                    });


        }
        monitorAppThread = new MonitorAppThread(this);
        monitorAppThread.start();

    }

    @Override
    public void onListChanged(TPListChangeMessage tpListChangeMessage) {

        LOGGER.log(FINE, String.format("listId: %s",tpListChangeMessage.listId));
        LOGGER.log(FINE, String.format("pluginId: %s",tpListChangeMessage.pluginId));
        LOGGER.log(FINE, String.format("actionId: %s",tpListChangeMessage.actionId));
        LOGGER.log(FINE, String.format("value: %s",tpListChangeMessage.value));
        LOGGER.log(FINE, String.format("instanceId: %s",tpListChangeMessage.instanceId));




        String listId = tpListChangeMessage.listId;
        String actionId = tpListChangeMessage.actionId;
        String value = tpListChangeMessage.value;
//        String instanceId = tpListChangeMessage.instanceId;

        //TODO: clean up all below with a switch statement?
        boolean isPowerLitraDeviceAction = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.powerLitraDevice");
        boolean isSetLEDColorForProfile = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.setLEDColorForProfile");
        boolean isSetBrightness = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.setBrightness");
        boolean isSetTemp = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.setTemperature");
        boolean isProfileBrightnessConnector = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileBrightnessConnector");
        boolean isProfileTemperatureConnector = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileTemperatureConnector");
        boolean isProfilePresetAction = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.enablePreset");
        boolean isSwitchActiveProfileAction = actionId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.switchActiveProfileForApp");


        if (isPowerLitraDeviceAction) {
            deviceProfileAppIf(listId,"powerLitraDevice",value,"action.");
        } else if (isSwitchActiveProfileAction) {
            String appListId = "com.kylergib.logighub.LogiGHubPlugin.LogiGHub.action.switchActiveProfileForApp.data.apps";
            LOGGER.log(Level.INFO, "isSwitchActiveProfileAction");
            if (listId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.profiles")) {
                List<String> appNames = sortProfileForApps(value);
                sendChoiceUpdate(appListId,
                        appNames.toArray(new String[0]));
            }

        } else if (isSetLEDColorForProfile) {
            deviceProfileAppIf(listId,"setLEDColorForProfile",value,"action.");
        } else if (isSetBrightness) {
            deviceProfileAppIf(listId,"setBrightness",value,"action.");
        } else if (isSetTemp) {
            deviceProfileAppIf(listId,"setTemperature",value,"action.");
        } else if (isProfileBrightnessConnector) {
            deviceProfileAppIf(listId,"profileBrightnessConnector",value,"connector.");
        } else if (isProfileTemperatureConnector) {
            deviceProfileAppIf(listId,"profileTemperatureConnector",value,"connector.");
        } else if (isProfilePresetAction) {
            deviceProfileAppIf(listId,"enablePreset",value,"action.");
        }

    }

    public void deviceProfileAppIf(String listId, String action, String value, String type) {
        String profileListId = "com.kylergib.logighub.LogiGHubPlugin.LogiGHub." + type + action + ".data.profiles";
        String appListId = "com.kylergib.logighub.LogiGHubPlugin.LogiGHub." + type + action + ".data.apps";
        boolean isDeviceState = listId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.devices");
        boolean isProfileList = listId.equals("com.kylergib.logighub.LogiGHubPlugin.LogiGHub." + type + action + ".data.profiles");
        if (isDeviceState) {
            List<LitraGlow> devices = GHubClient.devices.stream().filter(device -> device.getGivenName().equals(value)).collect(Collectors.toList());
            if (devices.size() == 1) {
                LitraGlow device = devices.get(0);
                List<String> profilesString = sortDeviceForProfiles(device);
                sendChoiceUpdate(profileListId,
                        profilesString.toArray(new String[0]));
            }
        } else if (isProfileList) {
            List<String> appNames = sortProfileForApps(value);
            sendChoiceUpdate(appListId,
                    appNames.toArray(new String[0]));


        }
    }

    public List<String> sortDeviceForProfiles(LitraGlow device) {
        List<Profile> allProfiles = device.getProfileList();
        List<String> profilesString = new ArrayList<>();
        allProfiles.forEach(profile -> {
            if (!profilesString.contains(profile.getName())) profilesString.add(profile.getName());
        });
        if (profilesString.size() == 1) profilesString.add("");
        return profilesString;
    }

    public List<String> sortProfileForApps(String value) {
        List<String> appNames = new ArrayList<>();
        GHubClient.profiles.forEach(profile -> {
            if (profile.getName().equals(value)) {
                List<Application> apps = GHubClient.apps.stream().filter(app -> app.getApplicationId().equals(profile.getApplicationId())).collect(Collectors.toList());
                apps.forEach(app -> {
                    if (!appNames.contains(app.getApplicationName())) appNames.add(app.getApplicationName());
                });
            }
        });
        if (appNames.size() == 1) appNames.add("");
        return appNames;
    }

    @Override
    public void onBroadcast(TPBroadcastMessage tpBroadcastMessage) {

    }

    @Override
    public void onSettings(TPSettingsMessage tpSettingsMessage) {
        setLogLevel();
    }

    @Override
    public void onNotificationOptionClicked(TPNotificationOptionClickedMessage tpNotificationOptionClickedMessage) {
        LOGGER.log(Level.INFO, "before Update option clicked");

        if (tpNotificationOptionClickedMessage.notificationId.equals(LogiGHubPluginConstants.LogiGHub.ID + ".updateNotification")) {
            if (tpNotificationOptionClickedMessage.optionId.equals(LogiGHubPluginConstants.LogiGHub.ID + ".updateNotification.options.openLink")) {

                LOGGER.log(Level.INFO, "Update option clicked");
                //TODO: redirect to github
                String url = "https://github.com/kylergib/TouchPortalLogiGHub";

                // Create a URI object from the URL
                URI uri = null;
                try {
                    uri = new URI(url);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                // Check if the Desktop API is supported on the current platform
                if (Desktop.isDesktopSupported()) {
                    // Get the desktop instance
                    Desktop desktop = Desktop.getDesktop();

                    // Check if the desktop can browse the URI
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        // Open the URL in the default browser
                        try {
                            desktop.browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }











    public static boolean isAppRunningWin() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("lghub")) {
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isAppRunningMac() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("lghub.app") && !(line.contains("update"))) {
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public void connectToGHub() throws URISyntaxException, InterruptedException {

        retrySent = false;

        needDevices = true;
        LOGGER.log(Level.FINE,"Starting plugin");


        latch = new CountDownLatch(1);
        currentIp = "localhost";
        setLogLevel();
        int port = 9010;
        LOGGER.log(Level.INFO, "Trying to connect to Log GHub at: " + currentIp + ":" + port);
        gHubClient = new GHubClient(currentIp,port,logiGHubPlugin);
        Thread.sleep(100);


        while (!gHubClient.isConnected) {
            if (gHubClient.isOpen()) continue;
            LOGGER.log(Level.WARNING, "Not connected to lghub");
            gHubClient = new GHubClient(currentIp,port,logiGHubPlugin);
            Thread.sleep(500);
        }
        latch.await();
//        latch = new CountDownLatch(5);
        msgCount = 150;
        gHubClient.send(Setup.pathAndVerbJson(null,"SUBSCRIBE", "/notifications"));
//        gHubClient.send(Setup.pathAndVerbJson(null,"SUBSCRIBE","/updates/frontend_restart_incoming"));
        gHubClient.send(Setup.pathAndVerbJson(null,"GET","/feature_flags"));
        gHubClient.send(Setup.pathAndVerbJson(null,"GET","/crash_reporting/status"));
        gHubClient.send(Setup.pathAndVerbJson(null,"GET","/updates/pipeline/info"));
        gHubClient.send(Setup.pathAndVerbJson(null,"GET","/community/lumen/environment"));
        subscribeToGHub();


    }
    public void setLogLevel() {
        LOGGER.log(Level.INFO, "Log level is: " + debugSetting);
        ConsoleHandler consoleHandler = (ConsoleHandler) Arrays.stream(LOGGER.getHandlers()).findFirst().get();
        Level newLevel;

        switch (debugSetting) {
            case 2:
                newLevel = Level.FINE;
                break;
            case 3:
                newLevel = Level.FINER;
                break;
            case 4:
                newLevel = Level.FINEST;
                break;
            default:
                newLevel = Level.INFO;
        }
        if (!consoleHandler.getLevel().equals(newLevel)) {
            consoleHandler.setLevel(newLevel);
            LOGGER.setLevel(newLevel);
            LOGGER.log(Level.INFO, "Set new logger level to: " + newLevel);
        }

    }

    @Override
    public void onBackendReceived() {

        getGhubSettings();


    }

    @Override
    public void onProfileAndDeviceReceived() {
        LOGGER.log(Level.FINE, "Received all configs, trying to sort");
        GHubClient.devices = new ArrayList<>();
        GHubClient.profiles = new ArrayList<>();
        GHubClient.apps = new ArrayList<>();
        GHubClient.presets = new ArrayList<>();

        if (DeviceSetup.createDevices()) {
            ProfileSetUp.sortProfiles();
            ApplicationSetUp.getApps();
            ProfileSetUp.getProfileSettings();
            ProfileSetUp.getActiveProfile();


            //after doing all configs


            //updates states
            List<String> allDevices = new ArrayList<>();
            GHubClient.devices.forEach(device -> {
                allDevices.add(device.getGivenName());
            });
            if (allDevices.size() == 1) allDevices.add("");
            sendChoiceUpdate(LogiGHubPluginConstants.LogiGHub.States.Devices.ID, allDevices.toArray(new String[0]));

            List<String> allProfiles = new ArrayList<>();
            GHubClient.profiles.forEach(profile -> {
                if (!allProfiles.contains(profile.getName())) allProfiles.add(profile.getName());
            });
            if (allProfiles.size() == 1) allProfiles.add("");
            sendChoiceUpdate(LogiGHubPluginConstants.LogiGHub.States.Profiles.ID, allProfiles.toArray(new String[0]));

            List<String> allPresets = new ArrayList<>();
            GHubClient.presets.forEach(preset ->{
                if (!allPresets.contains(preset.getPresetName())) allPresets.add(preset.getPresetName());
            });
            sendChoiceUpdate(LogiGHubPluginConstants.LogiGHub.States.Presets.ID, allPresets.toArray(new String[0]));
}
        else {
            if (!retrySent) {
                LOGGER.log(Level.WARNING, "Did not find devices, will retry");
                retrySent = true;
            }
            needDevices = true;
        }

    }

    public void subscribeToGHub() {
        gHubClient.send(Setup.getLocalPackages());
        gHubClient.send(Setup.setCommunityUser());
        gHubClient.send(Setup.pathAndVerbJson("3","SUBSCRIBE","/devices/state/changed"));
        gHubClient.send(Setup.pathAndVerbJson("4","SUBSCRIBE","/receivers/state/changed"));
        gHubClient.send(Setup.pathAndVerbJson("5","SUBSCRIBE","/receivers/pair"));
        gHubClient.send(Setup.pathAndVerbJson("6","SUBSCRIBE","/updates/status"));
        gHubClient.send(Setup.pathAndVerbJson("7","SUBSCRIBE","/input/mstate/changed"));
        gHubClient.send(Setup.pathAndVerbJson("8","SUBSCRIBE","/profiles/update"));
        gHubClient.send(Setup.pathAndVerbJson("9","SUBSCRIBE","/cards/updated"));
        gHubClient.send(Setup.pathAndVerbJson("10","SUBSCRIBE","/profile/active"));
        gHubClient.send(Setup.pathAndVerbJson("11","SUBSCRIBE","/profile/persistent"));
        gHubClient.send(Setup.pathAndVerbJson("12","SUBSCRIBE","/profiles/persistent_features"));
        gHubClient.send(Setup.pathAndVerbJson("13","SUBSCRIBE","/lighting/turn_off_for_inactivity"));
        gHubClient.send(Setup.pathAndVerbJson("14","SUBSCRIBE","/integration_manager/settings/led_sdk_enabled"));
        gHubClient.send(Setup.pathAndVerbJson("15","SUBSCRIBE","/applications"));
        gHubClient.send(Setup.pathAndVerbJson("16","SUBSCRIBE","gshift"));
        gHubClient.send(Setup.pathAndVerbJson("17","SUBSCRIBE","/community/user/me"));
        gHubClient.send(Setup.pathAndVerbJson("18","SUBSCRIBE","scripts"));
        gHubClient.send(Setup.pathAndVerbJson("19","SUBSCRIBE","/api/v1/integrations"));
        gHubClient.send(Setup.pathAndVerbJson("20","SUBSCRIBE","/api/v1/integrations/active/instances"));
        gHubClient.send(Setup.pathAndVerbJson("21","SUBSCRIBE","/overwolf/extensions"));
        gHubClient.send(Setup.pathAndVerbJson("22","SUBSCRIBE","/community/publish/progress"));
        gHubClient.send(Setup.pathAndVerbJson("23","SUBSCRIBE","/battery/state/changed"));
        gHubClient.send(Setup.pathAndVerbJson("24","SUBSCRIBE","/lighting/palette/vibrant"));
        gHubClient.send(Setup.pathAndVerbJson("25","SUBSCRIBE","/lighting/palette/pastel"));
        gHubClient.send(Setup.pathAndVerbJson("26","SUBSCRIBE","/lighting/palette/mono"));
        gHubClient.send(Setup.pathAndVerbJson("27","SUBSCRIBE","/lighting/palette/custom"));
        gHubClient.send(Setup.pathAndVerbJson("28","SUBSCRIBE","/scarif/status"));
        gHubClient.send(Setup.pathAndVerbJson("29","SUBSCRIBE","/lighting/effect/save"));
        gHubClient.send(Setup.pathAndVerbJson("30","SUBSCRIBE","/lighting/effect/delete"));
        gHubClient.send(Setup.pathAndVerbJson("31","SUBSCRIBE","/configuration"));
        gHubClient.send(Setup.pathAndVerbJson("32","SUBSCRIBE","/settings/error"));
        gHubClient.send(Setup.pathAndVerbJson("33","SUBSCRIBE","/migration/process"));
        gHubClient.send(Setup.pathAndVerbJson("34","SUBSCRIBE","/dfu/progress"));
        gHubClient.send(Setup.pathAndVerbJson("35","SUBSCRIBE","/settings/error"));
        gHubClient.send(Setup.pathAndVerbJson("36","SUBSCRIBE","/driver_registry/restart_device"));
        gHubClient.send(Setup.pathAndVerbJson("37","SUBSCRIBE","/updates/periodic_check"));
        gHubClient.send(Setup.pathAndVerbJson("38","SUBSCRIBE","/updates/channel"));
        gHubClient.send(Setup.pathAndVerbJson("39","SUBSCRIBE","/microphone/polar_pattern/changed"));
        gHubClient.send(Setup.pathAndVerbJson("40","SUBSCRIBE","/microphone/mode/changed"));
        gHubClient.send(Setup.pathAndVerbJson("41","SUBSCRIBE","/notifications"));
        gHubClient.send(Setup.pathAndVerbJson("42","SUBSCRIBE","/devices/driver_info"));
        gHubClient.send(Setup.pathAndVerbJson("43","SUBSCRIBE","/notifications/config"));
        gHubClient.send(Setup.pathAndVerbJson("44","SUBSCRIBE","/notifications/audio_prompt/languanges"));
    }

    public void getGhubSettings() {
        gHubClient.send(Setup.pathAndVerbJson("45","GET","/updates/periodic_check"));
        gHubClient.send(Setup.pathAndVerbJson("46","GET","/crash_reporting/status"));
        gHubClient.send(Setup.pathAndVerbJson("47","GET","/updates/status"));
        gHubClient.send(Setup.pathAndVerbJson("48","GET","/configuration"));
        gHubClient.send(Setup.pathAndVerbJson("49","GET","/legacy_profiles_exist"));
        gHubClient.send(Setup.pathAndVerbJson("50","GET","/devices/list"));
        gHubClient.send(Setup.pathAndVerbJson("51","GET","/receivers/list"));
        gHubClient.send(Setup.pathAndVerbJson("52","GET","/profiles"));
        gHubClient.send(Setup.pathAndVerbJson("53","GET","/api/v1/integrations"));
        gHubClient.send(Setup.pathAndVerbJson("54","GET","/api/v1/integrations/active/instances"));
        gHubClient.send(Setup.pathAndVerbJson("55","GET","/overwolf/extensions"));
        gHubClient.send(Setup.pathAndVerbJson("56","GET","/cards", true));
        gHubClient.send(Setup.pathAndVerbJson("57","GET","/applications"));
        gHubClient.send(Setup.pathAndVerbJson("58","GET","/profile/active"));
        gHubClient.send(Setup.pathAndVerbJson("59","GET","/profile/persistent"));
        gHubClient.send(Setup.pathAndVerbJson("60","GET","/profiles/persistent_features"));
        gHubClient.send(Setup.pathAndVerbJson("61","GET","/lighting/turn_off_for_inactivity"));
        gHubClient.send(Setup.pathAndVerbJson("62","GET","/integration_manager/settings/led_sdk_enabled"));
        gHubClient.send(Setup.pathAndVerbJson("63","GET","/lighting/palette/vibrant"));
        gHubClient.send(Setup.pathAndVerbJson("64","GET","/lighting/palette/pastel"));
        gHubClient.send(Setup.pathAndVerbJson("65","GET","/lighting/palette/mono"));
        gHubClient.send(Setup.pathAndVerbJson("66","GET","/lighting/palette/custom"));
        gHubClient.send(Setup.pathAndVerbJson("67","GET","/lighting/effect/prefabs"));
        gHubClient.send(Setup.pathAndVerbJson("68","GET","/updates/pipeline/info"));
        gHubClient.send(Setup.pathAndVerbJson("69","GET","/updates/channel"));
        gHubClient.send(Setup.pathAndVerbJson("70","GET","gshift"));
        gHubClient.send(Setup.pathAndVerbJson("71","GET","/community/query/defaults"));
        gHubClient.send(Setup.pathAndVerbJson("72","GET","/devices/model/info"));
        gHubClient.send(Setup.pathAndVerbJson("73","GET","/resources/release_notes"));
        gHubClient.send(Setup.pathAndVerbJson("74","GET","/notifications/config"));
        gHubClient.send(Setup.pathAndVerbJson("75","GET","/notifications/audio_prompt/languages"));
        gHubClient.send(Setup.pathAndVerbJson("76","GET","/scarif/status"));
    }

    //Actions
    @Action(description = "Turn device on/off or toggle", format = "Set device: {$devices$} to {$values$} for profile: {$profiles$} and app: {$apps$}",
            categoryId = "LogiGHub", name="Power device on/off")
    public static void powerLitraDevice(@Data(stateId = "devices")  String[] devices,
                                        @Data(valueChoices = {}) String[] profiles,
                                        @Data(valueChoices = {}) String[] apps,
                                        @Data(valueChoices = {"on","off","toggle"}) String[] values) {
        if (!appIsOpen) return;
        LogiGHubPlugin.LOGGER.log(Level.FINE, String.format("device: %s, profile: %s, app: %s, value: %s",
                devices[0],profiles[0],apps[0],values[0]));
        //finds app id then gets the profile and then finds the device that it needs to change power to.
        Application application = GHubClient.apps.stream().filter(app -> app.getApplicationName().equals(apps[0])).findFirst().get();
        Profile selectedProfile = GHubClient.profiles.stream().filter(profile ->
                (profile.getName().equals(profiles[0]) && profile.getApplicationId().equals(application.getApplicationId()))).findFirst().get();
        LitraGlow litraGlow = GHubClient.devices.stream().filter(device ->
                        device.getGivenName().equals(devices[0]) && device.getProfileList().contains(selectedProfile))
                .findFirst().get();
        boolean newValue;
        switch (values[0]) {
            case "on":
                newValue = true;
                break;
            case "off":
                newValue = false;
                break;
            case "toggle":
                newValue = !selectedProfile.isPoweredOn();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + values[0]);
        }

        LogiGHubPlugin.gHubClient.send(
                LitraGlowActions.powerLitraDevice(litraGlow,selectedProfile,newValue));
        selectedProfile.setPoweredOn(newValue);

    }


    @Action(description = "Switch active profile for specific app", format = "Set profile: {$profiles$} active for app: {$apps$}",
            categoryId = "LogiGHub", name="Switch Profile")
    public static void switchActiveProfileForApp(
            @Data(stateId = "profiles") String[] profiles,
            @Data(valueChoices = {}) String[] apps) {
        if (!appIsOpen) return;
        Application selectedApp = findApp(apps[0]);
        if (selectedApp != null) {
            Profile selectedProfile = findProfileWithApp(GHubClient.profiles, profiles[0], selectedApp.getApplicationId());
            if (selectedProfile != null) {
                String activeProfileString = LitraGlowActions.switchActiveProfileForApp(selectedProfile);
                gHubClient.send(activeProfileString);
            }
        }



    }

    @Action(description = "Set Brightness for device on profile/app (brightness is from 0-100, 0 sets brightness to lowest amount in G Hub", format = "Device: {$devices$}, profile: {$profiles$} for app: {$apps$}, brightness: {$brightness$}",
            categoryId = "LogiGHub", name="Set Brightness")
    public static void setBrightness(@Data(stateId = "devices")  String[] devices,
                                             @Data(valueChoices = {}) String[] profiles,
                                             @Data(valueChoices = {}) String[] apps,
                                             @Data(minValue = 0, maxValue = 100) Integer brightness) {
        if (!appIsOpen) return;

        int newBrightness = convertPercentToBrightness(brightness);

        LOGGER.log(Level.FINE, String.format("brightness %d", newBrightness));

        Application selectedApp = findApp(apps[0]);

        LitraGlow selectedDevice = findDevice(devices[0]);
        Profile selectedProfile = findProfileWithApp(selectedDevice.getProfileList(),profiles[0], selectedApp.getApplicationId());
        if (selectedProfile != null) {
            if (selectedProfile.getBrightness() != brightness) {
                selectedProfile.setBrightness(brightness);
                String ledString = LitraGlowActions.setLEDColorForProfileNoPreset(selectedDevice, selectedProfile, selectedApp,
                        selectedProfile.getTemperature(), newBrightness);
                String assignmentString = LitraGlowActions.setLEDColorForProfileWithPreset(selectedDevice, selectedProfile, selectedApp,
                        selectedProfile.getTemperature(), newBrightness);
                selectedProfile.setPresetActive(false);

                gHubClient.send(ledString);
                gHubClient.send(assignmentString);
                setBrightnessConnector(selectedProfile, brightness,selectedDevice.getGivenName(), selectedApp);



            }

        }

    }
    @Action(description = "Set Temperature for device on profile/app (temperature is from 2700 - 6500, anything below 2700 will set it to 2700)", format = "Device: {$devices$}, profile: {$profiles$} for app: {$apps$}, temperature: {$temp$}",
            categoryId = "LogiGHub", name="Set Temperature")
    public static void setTemperature(@Data(stateId = "devices")  String[] devices,
                                     @Data(valueChoices = {}) String[] profiles,
                                     @Data(valueChoices = {}) String[] apps,
                                     @Data(minValue = 0, maxValue = 6500) Integer temp) {
        if (!appIsOpen) return;
        if (temp < 2700)  temp = 2700;
        else temp =  Math.round(temp / 100.0f) * 100;
        LOGGER.log(Level.FINE, String.format("temp %d", temp));
        Application selectedApp = findApp(apps[0]);
        LOGGER.log(Level.FINE, "1");
        LitraGlow selectedDevice = findDevice(devices[0]);
        Profile selectedProfile = findProfileWithApp(selectedDevice.getProfileList(),profiles[0], selectedApp.getApplicationId());
        LOGGER.log(Level.FINE, "2");
        if (selectedProfile != null) {
            LOGGER.log(Level.FINE, "10");
            String ledString = LitraGlowActions.setLEDColorForProfileNoPreset(selectedDevice, selectedProfile, selectedApp,
                    temp, convertPercentToBrightness(selectedProfile.getBrightness()));
            LOGGER.log(Level.FINE, "9");
            gHubClient.send(ledString);
            LOGGER.log(Level.FINE, "3");
            String assignmentString = "";
            assignmentString = LitraGlowActions.setLEDColorForProfileWithPreset(selectedDevice, selectedProfile, selectedApp,
                    temp, convertPercentToBrightness(selectedProfile.getBrightness()));
            gHubClient.send(assignmentString);
            LOGGER.log(Level.FINE, "4");
            selectedProfile.setPresetActive(false);
            LOGGER.log(Level.FINE, "5");
            selectedProfile.setTemperature(temp);
            setTemperatureConnector(selectedProfile, temp, selectedDevice.getGivenName(), selectedApp);
            LOGGER.log(Level.FINE, "6");



        }


    }

    @Action(description = "Enable preset for device on profile/app", format = "Device: {$devices$}, profile: {$profiles$} for app: {$apps$}, preset: {$presets$}",
            categoryId = "LogiGHub", name="Set Preset")
    public static void enablePreset(@Data(stateId = "devices")  String[] devices,
                                      @Data(valueChoices = {}) String[] profiles,
                                      @Data(valueChoices = {}) String[] apps,
                                      @Data(stateId = "presets") String[] presets) {

        if (!appIsOpen) return;
        LOGGER.log(Level.FINE, String.format("preset %s", presets[0]));
        Application selectedApp = findApp(apps[0]);

        LitraGlow selectedDevice = findDevice(devices[0]);
        Profile selectedProfile = null;
        int convertedBrightness = 0;
        Preset selectedPreset = findPreset(GHubClient.presets, presets[0]);
        if (selectedDevice != null && selectedApp != null && selectedPreset != null) {
            selectedProfile = findProfileWithApp(selectedDevice.getProfileList(),profiles[0], selectedApp.getApplicationId());
            convertedBrightness = (int) convertBrightnessToPercentage(selectedPreset.getBrightness());
            String presetString = LitraGlowActions.enablePresetForProfile(selectedDevice,selectedProfile,selectedPreset,selectedApp);
            gHubClient.send(presetString);
            selectedProfile.setTemperature(selectedPreset.getTemperature());
            LOGGER.log(FINE, "SET TEMP TO: " + selectedProfile.getTemperature());
            selectedProfile.setBrightness(convertedBrightness);
            selectedProfile.setPresetActive(true);
            setTemperatureConnector(selectedProfile, selectedPreset.getTemperature(),
                    selectedDevice.getGivenName(),selectedApp);
            setBrightnessConnector(selectedProfile,convertedBrightness,
                    selectedDevice.getGivenName(),selectedApp);
        }






    }

    @Connector(format = "Device: {$devices$}, profile: {$profiles$} for app: {$apps$}",
            categoryId = "LogiGHub", name="Profile Brightness",id="profileBrightnessConnector")
    private void profileBrightnessConnector(@Data(stateId = "devices")  String[] devices,
                                       @Data(valueChoices = {}) String[] profiles,
                                       @Data(valueChoices = {}) String[] apps,
                                       @ConnectorValue Integer brightness) {
        if (!appIsOpen) return;
        int newBrightness = convertPercentToBrightness(brightness);

        LOGGER.log(Level.FINE, String.format("brightness %d", newBrightness));
        Application selectedApp = findApp(apps[0]);

        LitraGlow selectedDevice = findDevice(devices[0]);
        Profile selectedProfile = findProfileWithApp(selectedDevice.getProfileList(),profiles[0], selectedApp.getApplicationId());
        if (selectedProfile != null) {

            String ledString = LitraGlowActions.setLEDColorForProfileNoPreset(selectedDevice, selectedProfile, selectedApp,
                    selectedProfile.getTemperature(), newBrightness);
            String assignmentString = LitraGlowActions.setLEDColorForProfileWithPreset(selectedDevice, selectedProfile, selectedApp,
                    selectedProfile.getTemperature(), newBrightness);
            selectedProfile.setPresetActive(false);

            gHubClient.send(ledString);
            gHubClient.send(assignmentString);
            selectedProfile.setBrightness(brightness);

        }

    }

    @Connector(format = "Device: {$devices$}, profile: {$profiles$} for app: {$apps$}",
            categoryId = "LogiGHub", name="Profile Temperature",id="profileTemperatureConnector")
    private void profileTemperatureConnector(@Data(stateId = "devices")  String[] devices,
                                            @Data(valueChoices = {}) String[] profiles,
                                            @Data(valueChoices = {}) String[] apps,
                                            @ConnectorValue Integer temp) {
        if (!appIsOpen) return;
        int convertedTemp = convertPercentToTemperature(temp);

        LOGGER.log(Level.FINE, String.format("temp %d", convertedTemp));
        Application selectedApp = findApp(apps[0]);

        LitraGlow selectedDevice = findDevice(devices[0]);
        Profile selectedProfile = findProfileWithApp(selectedDevice.getProfileList(),profiles[0], selectedApp.getApplicationId());
        if (selectedProfile != null) {
            selectedProfile.setTemperature(convertedTemp);
            String ledString = LitraGlowActions.setLEDColorForProfileNoPreset(selectedDevice, selectedProfile, selectedApp,
                    convertedTemp, convertPercentToBrightness(selectedProfile.getBrightness()));
            String assignmentString = LitraGlowActions.setLEDColorForProfileWithPreset(selectedDevice, selectedProfile, selectedApp,
                    convertedTemp, convertPercentToBrightness(selectedProfile.getBrightness()));
            selectedProfile.setPresetActive(false);

            gHubClient.send(ledString);
            gHubClient.send(assignmentString);
            selectedProfile.setTemperature(temp);

        }

    }



    public static Preset findPreset(List<Preset> presetList, String presetName) {
        List<Preset> filteredPresets = presetList.stream().
                filter(preset -> preset.getPresetName().equals(presetName)).collect(Collectors.toList());
        if (filteredPresets.size() == 1) return filteredPresets.get(0);
        else return null;
    }


    public static Profile findProfileWithApp(List<Profile> profileList,String profileValue, String appId) {
        List<Profile> filteredProfiles = profileList.stream().
                filter(profile -> profileValue.equals(profile.getName()) &&
                        profile.getApplicationId().equals(appId)
                ).
                collect(Collectors.toList());
        if (filteredProfiles.size() == 1) return filteredProfiles.get(0);
        else return null;
    }
    public static LitraGlow findDevice(String deviceValue) {
        List<LitraGlow> filteredDevices = GHubClient.devices.stream().filter(device -> device.getGivenName().equals(deviceValue)).collect(Collectors.toList());
        if (filteredDevices.size() == 1) return filteredDevices.get(0);
        else return null;
    }
    public static LitraGlow findDeviceBySignature(String deviceValue) {
        List<LitraGlow> filteredDevices = GHubClient.devices.stream().filter(device -> device.getDeviceSignature().equals(deviceValue)).collect(Collectors.toList());
        if (filteredDevices.size() == 1) return filteredDevices.get(0);
        else return null;
    }
    public static Application findApp(String appValue) {
        List<Application> filteredApps = GHubClient.apps.stream().filter(app -> app.getApplicationName().equals(appValue)).collect(Collectors.toList());
        if (filteredApps.size() == 1) return filteredApps.get(0);
        else return null;
    }
    public static Application findAppById(String appValue) {
        List<Application> filteredApps = GHubClient.apps.stream().filter(app -> app.getApplicationId().equals(appValue)).collect(Collectors.toList());
        if (filteredApps.size() == 1) return filteredApps.get(0);
        else return null;
    }

    public static int convertPercentToBrightness(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        return Math.round((Float.valueOf(percent) / 100) * (250 - 20) + 20);
    }
    public static double convertBrightnessToPercentage(int value) {
        if (value < 20 || value > 250) {
            throw new IllegalArgumentException("Value must be between 20 and 250");
        }
        return ((double) (value - 20) / (250 - 20)) * 100;
    }
    public static int convertPercentToTemperature(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        int temp = Math.round((Float.valueOf(percent) / 100) * (6500 - 2700) + 2700);
        temp = Math.round(temp / 100.0f) * 100;
        if (temp > 6500) temp = 6500;
        else if (temp < 2700) temp = 2700;
        return temp;
    }
    public static double convertTemperatureToPercentage(int value) {
        if (value < 2700 || value > 6500) {
            throw new IllegalArgumentException("Value must be between 20 and 250");
        }
        return ((double) (value - 2700) / (6500 - 2700)) * 100;
    }
    public boolean checkForUpdate() {
        //TODO: replace with right info after commit
        String repositoryOwner = "kylergib";
        String repositoryName = "TouchPortalLogiGHub";

        try {
            URL url = new URL("https://api.github.com/repos/" + repositoryOwner + "/" + repositoryName + "/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            StringBuilder response = new StringBuilder();

            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();
            JSONObject responseJSON = new JSONObject(response.toString());

            List<String> version = Arrays.asList(responseJSON.getString("tag_name").split("\\."));
            int newestVersion = 0;
            if (version.size() == 3) {
                newestVersion = (Integer.valueOf(version.get(0)) * 1000) +
                        (Integer.valueOf(version.get(1)) * 100) +
                        (Integer.valueOf(version.get(2)));
            }
            int currentVersion = Integer.valueOf(String.valueOf(BuildConfig.VERSION_CODE));

            LOGGER.log(INFO, "Newest version available is: " + newestVersion);
            LOGGER.log(INFO, "Current version is: " + currentVersion);
            return currentVersion < newestVersion;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void setBrightnessConnector(Profile selectedProfile, int brightness, String givenName, Application app) {
        HashMap<String, Object> brightnessData = new HashMap<>();

        brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileBrightnessConnector.data.profiles", selectedProfile.getName());
        brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileBrightnessConnector.data.apps", app.getApplicationName());
        brightnessData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.devices", givenName);
        LogiGHubPlugin.logiGHubPlugin.sendConnectorUpdate(LogiGHubPluginConstants.ID,
                LogiGHubPluginConstants.LogiGHub.Connectors.ProfileBrightnessConnector.ID, brightness, brightnessData);
    }

    public static void setTemperatureConnector(Profile selectedProfile, int temp, String givenName, Application app) {
        HashMap<String, Object> temperatureData = new HashMap<>();

        int sliderTemp = Math.toIntExact(Math.round(convertTemperatureToPercentage(temp)));
        temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileTemperatureConnector.data.profiles", selectedProfile.getName());
        temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.connector.profileTemperatureConnector.data.apps", app.getApplicationName());
        temperatureData.put("com.kylergib.logighub.LogiGHubPlugin.LogiGHub.state.devices", givenName);

        LogiGHubPlugin.logiGHubPlugin.sendConnectorUpdate(LogiGHubPluginConstants.ID,
                LogiGHubPluginConstants.LogiGHub.Connectors.ProfileTemperatureConnector.ID, sliderTemp, temperatureData);
    }


}