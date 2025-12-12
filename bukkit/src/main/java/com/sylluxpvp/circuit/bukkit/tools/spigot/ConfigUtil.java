package com.sylluxpvp.circuit.bukkit.tools.spigot;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ConfigUtil {

    private final File pluginDataFolder = CircuitPlugin.getInstance().getDataFolder();
    private final Map<YamlConfiguration, String> configMap = new HashMap<>();

    /**
     * Creates a new configuration file with the given name.
     * If the file already exists, it will not be overwritten.
     *
     * @param name the name of the configuration file (without .yml extension)
     * @return the YamlConfiguration object
     */
    public YamlConfiguration createConfig(String name) {
        return createConfig(pluginDataFolder, name);
    }

    public YamlConfiguration createConfig(File parentFolder, String name) {
        File configFile = new File(parentFolder, name + ".yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                String relativePath = pluginDataFolder.toPath().relativize(configFile.toPath())
                        .toString()
                        .replace(File.separatorChar, '/'); // necessário pro getResource

                try (InputStream resourceStream = CircuitPlugin.getInstance().getResource(relativePath)) {
                    if (resourceStream != null) {
                        Files.copy(resourceStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        configFile.createNewFile();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration configuration = loadConfig(parentFolder, name);
        configMap.put(configuration, name);
        return configuration;
    }

    /**
     * Loads an existing configuration file.
     *
     * @param name the name of the configuration file (without .yml extension)
     * @return the YamlConfiguration object
     */
    public YamlConfiguration loadConfig(String name) {
        return loadConfig(pluginDataFolder, name);
    }

    /**
     * Loads an existing configuration file.
     *
     * @param name the name of the configuration file (without .yml extension)
     * @return the YamlConfiguration object
     */
    public YamlConfiguration loadConfig(File parent, String name) {
        File configFile = new File(parent, name + ".yml");
        if (!configFile.exists()) {
            return null;
        }
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            saveConfig(parent, config, name);
        }
        return config;
    }

    /**
     * Saves the given YamlConfiguration to a file with the given name.
     *
     * @param config the YamlConfiguration object
     * @param name   the name of the configuration file (without .yml extension)
     */
    public void saveConfig(YamlConfiguration config, String name) {
        saveConfig(pluginDataFolder, config, name);
    }

    public void saveConfig(File parent, YamlConfiguration config, String name) {
        File configFile = new File(parent, name + ".yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the given YamlConfiguration
     *
     * @param config the YamlConfiguration object
     */
    public void saveConfig(YamlConfiguration config) {
        saveConfig(pluginDataFolder, config);
    }

    public void saveConfig(File parent, YamlConfiguration config) {
        String name = configMap.getOrDefault(config, config.getName());
        File configFile = new File(parent, name + ".yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Deletes the configuration file with the given name.
     *
     * @param name the name of the configuration file (without .yml extension)
     * @return true if the file was successfully deleted, false otherwise
     */
    public boolean deleteConfig(String name) {
        return deleteConfig(pluginDataFolder, name);
    }

    public boolean deleteConfig(File parent, String name) {
        File configFile = new File(parent, name + ".yml");
        return configFile.delete();
    }
}
