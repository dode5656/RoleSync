package io.github.dode5656.rolesync.storage;

import io.github.dode5656.rolesync.RoleSync;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileStorage {
    private final File file;
    private FileConfiguration fileStorage;

    public FileStorage(String name, File location) {
        this.file = new File(location, name);
        reload();
    }

    public final void save(RoleSync main) {
        Logger logger = main.getLogger();
        try {
            fileStorage.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save " + file.getName() + " file!", e);
        }
    }

    public final FileConfiguration read() {
        return fileStorage;
    }

    public final void reload() {
        this.fileStorage = YamlConfiguration.loadConfiguration(this.file);
    }

    public final void saveDefaults(RoleSync main) {

        if (this.file.exists()) {
            FileConfiguration tempConfig = new YamlConfiguration();
            try {
                tempConfig.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                main.getLogger().log(Level.SEVERE, "Couldn't load " + file.getName(), e);
            }

            if (tempConfig.getString("version") != null &&
                    tempConfig.getString("version").equals(main.getDescription().getVersion())) {
                reload();
                return;
            }

            File oldDir = new File(main.getDataFolder().getPath(),"old");

            if (!oldDir.exists()) {
                oldDir.mkdirs();
            }

            this.file.renameTo(new File(main.getDataFolder().getPath()+File.separator+"old",
                    "old_"+this.file.getName()));

        }

        main.saveResource(this.file.getName(), false);
        reload();

    }

}