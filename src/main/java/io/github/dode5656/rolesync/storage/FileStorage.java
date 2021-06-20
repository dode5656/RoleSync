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
    private final Logger logger;

    public FileStorage(String name, File location, RoleSync main) {
        this.file = new File(location, name);
        this.logger = main.getLogger();
    }

    public final void save() {
        try {
            fileStorage.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save " + file.getName() + " file!", e);
        }
    }

    public final FileConfiguration read() {
        reload();
        return fileStorage;
    }

    public final void reload() {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) file.createNewFile();
            this.fileStorage = new YamlConfiguration();
            this.fileStorage.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Couldn't load "+ file.getName(), e);
        }
    }

    public final void saveDefaults(RoleSync main) {
        if (!main.getUtil().saveDefaults(file, this)) {
            main.saveResource(this.file.getName(), false);
            reload();
        }
    }

}