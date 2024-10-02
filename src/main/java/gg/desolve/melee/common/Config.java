package gg.desolve.melee.common;

import gg.desolve.melee.Melee;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

@Getter
public class Config {

    private final File file;
    private FileConfiguration config;

    public Config(String name) {
        this.file = new File(Melee.getInstance().getDataFolder(), name);
        this.config = new YamlConfiguration();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            Melee.getInstance().saveResource(name, true);
        }

        load();
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    private void load() {
        try {
            config.load(file);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + file.getName() + ".");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving " + file.getName() + ".");
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            config = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem reloading " + file.getName() + ".");
            e.printStackTrace();
        }
    }
}
