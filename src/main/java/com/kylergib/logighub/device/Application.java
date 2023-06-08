package com.kylergib.logighub.device;

public class Application {
    private String applicationId;
    private String applicationFolder;
    private String applicationName;
    private Profile activeProfile;

    public Application(String applicationId, String applicationFolder, String applicationName) {
        this.applicationId = applicationId;
        this.applicationFolder = applicationFolder;
        this.applicationName = applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationFolder() {
        return applicationFolder;
    }

    public void setApplicationFolder(String applicationFolder) {
        this.applicationFolder = applicationFolder;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
