package com.kylergib.logighub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static com.kylergib.logighub.LogiGHubPlugin.LOGGER;

public class MonitorAppThread extends Thread {
    private final AppOpenCallback appCallback;
    private volatile boolean stopRequested = false;

    public void requestStop() {
        stopRequested = true;
        interrupt(); // Optional: Interrupt the thread if it's blocked.
    }

    public MonitorAppThread(AppOpenCallback appCallback) {
        this.appCallback = appCallback;
    }

    public interface AppOpenCallback {
        void onAppOpened();
        void onAppClosed();
    }

    public void run() {
        // Code to be executed in the new thread
        String os = System.getProperty("os.name").toLowerCase();
        LOGGER.log(Level.INFO, os);
        boolean isRunning = false;
        if (os.contains("win")) {

            isRunning = isAppRunningWin();
        } else if (os.contains("mac")) {

            isRunning = isAppRunningMac();
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
        while (!stopRequested) {
            int retryCountNeeded = 100;
            if (retries % retryCountNeeded == 0) LOGGER.log(Level.FINEST, "Checking if Logitech G Hub is open");
            if (os.contains("win")) {

                isRunning = isAppRunningWin();
            } else if (os.contains("mac")) {

                isRunning = isAppRunningMac();
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

    private boolean isAppRunningMac() {
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
