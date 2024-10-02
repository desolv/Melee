package gg.desolve.melee;

import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.common.Configuration;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Profile;
import gg.desolve.melee.storage.MeleeMongoManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    private Configuration storageConfig;

    @Getter
    @Setter
    private MeleeMongoManager mongoManager;

    @Override
    public void onEnable() {
        instance = this;

        storageConfig = new Configuration("storage.yml");

        new MeleeMongoManager(this);
        new MeleeListenerManager(this);
        new MeleeCommandManager(this);
    }

    @Override
    public void onDisable() {
        Profile.getProfiles().forEach((uuid, profile) -> {
            profile.setLastSeen(System.currentTimeMillis());
            profile.save();
        });
        mongoManager.getMongoClient().close();
    }

}
