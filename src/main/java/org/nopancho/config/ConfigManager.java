package org.nopancho.config;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.palladian.helper.io.FileHelper;

import java.io.File;

/**
 * Created by Sebastian on 24.11.2017.
 *
 * A Utility class that allows to read settings from property file
 */
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private static PropertiesConfiguration config = null;

    public static PropertiesConfiguration getConfig(){
        return getConfig(false);
    }

    public static PropertiesConfiguration getConfig(boolean reload) {
        String configDirectory;
        try {
            boolean configExists = FileHelper.directoryExists("config");
            if(configExists) {
                configDirectory = "config";
            } else {
                configDirectory = "../config";
            }
        } catch (Exception e) {
            configDirectory = "config/";
        }

        if (config == null || reload) {
            try {
                if (FileHelper.fileExists(configDirectory + "/core.properties")) {
                    config = new PropertiesConfiguration(new File(configDirectory, "core.properties"));
                } else {
                    config = new PropertiesConfiguration(new File(configDirectory, "core.properties.default"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return config;
    }
}
