package gg.desolve.melee;

import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.common.Configuration;
import gg.desolve.melee.database.MeleeMongoManager;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Profile;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    private Configuration databaseConfig;

    @Getter
    private static MeleeMongoManager mongoManager;

    @Override
    public void onEnable() {
        instance = this;

        databaseConfig = new Configuration("database.yml");

        mongoManager = new MeleeMongoManager(this);
        new MeleeListenerManager(this);
        new MeleeCommandManager(this);
    }

    @Override
    public void onDisable() {
        Profile.getProfiles().forEach((uuid, profile) -> profile.save());
        mongoManager.getMongoClient().close();
    }

}
