package com.kylergib.logighub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;

public class GHubLogger {
    public static GHubLogger gLogger;
    public String folderPath = "customLog/";
    public String fileName;

    public GHubLogger() {
        fileName = "custom-output" + LocalDateTime.now() + ".txt";
        createFolder();
    }

    public void createFolder() {
        if (LogiGHubPlugin.debugSetting > 3) {

            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    LogiGHubPlugin.LOGGER.log(Level.WARNING, "Failed to create the folder.");
                    return;
                } else LogiGHubPlugin.LOGGER.log(Level.INFO,"Created folder: " + folder.getAbsolutePath());

            }
            try (FileWriter fileWriter = new FileWriter(folderPath + File.separator + fileName)) {
                fileWriter.write(String.valueOf(LocalDateTime.now()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public boolean addMessage(String message) {
        String current = LocalDateTime.now().toString();
            try (FileWriter fileWriter = new FileWriter(folderPath + File.separator + fileName,true)) {
                fileWriter.write("(" + current + "): " + message + "\n");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

    }
}
