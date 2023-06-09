package com.kylergib.logighub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static com.kylergib.logighub.LogiGHubPlugin.LOGGER;

public class MonitorAppThread extends Thread {
    private final AppOpenCallback appCallback;
    private volatile boolean stopRequested = false;
    private final String os;

    public void requestStop() {
        stopRequested = true;
        interrupt();
    }

    public MonitorAppThread(AppOpenCallback appCallback) {
        this.appCallback = appCallback;
        os = System.getProperty("os.name").toLowerCase();
    }

    public interface AppOpenCallback {
        void onAppOpened();
        void onAppClosed();
    }

    public void run() {
        // Code to be executed in the new thread

        LOGGER.log(Level.INFO, os);
        boolean isRunning = false;
        if (os.contains("win")) {

            isRunning = isAppRunningWin();
        } else if (os.contains("mac")) {

            isRunning = isAppRunningMac("lghub");
        }
        boolean appOpenedPreviously = isRunning;
        if (!isRunning) {
            LOGGER.log(Level.WARNING, "Logitech G Hub is not running");
        }
        else {
            LOGGER.log(Level.INFO, "Logitech G Hub is running.");
            appCallback.onAppOpened();
        }
        int retries = 0;
        int retryCountNeeded = 100;
        while (!stopRequested) {

            if (retries % retryCountNeeded == 0) LOGGER.log(Level.INFO, "Checking if Logitech G Hub is open");
            if (os.contains("win")) {

                isRunning = isAppRunningWin();

            } else if (os.contains("mac")) {

                isRunning = isAppRunningMac("lghub");
            }
            if (appOpenedPreviously && !isRunning) {
                if (retries % retryCountNeeded == 0) LOGGER.log(Level.FINEST, "Logitech G Hub has closed");
                appCallback.onAppClosed();
                appOpenedPreviously = false;
            } else if (!appOpenedPreviously && isRunning) {
                if (retries % retryCountNeeded == 0) LOGGER.log(Level.FINEST, "Logitech G Hub has opened");
                appOpenedPreviously = isRunning;
                appCallback.onAppOpened();
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            retries += 1;
        }
    }
    public static boolean isAppRunningMac(String appName) {

        try {
            Process process = Runtime.getRuntime().exec("pgrep -l " + appName );


            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("lghub") && !(line.contains("update"))) {
                    process.destroy();
                    return true;
                }
            }
            process.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    private boolean isAppRunningWin() {
        try {
            //TODO: need to test
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
}
