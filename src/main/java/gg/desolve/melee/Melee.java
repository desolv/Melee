package gg.desolve.melee;

import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.common.Config;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Profile;
import gg.desolve.melee.rank.MeleeRankManager;
import gg.desolve.melee.storage.MeleeMongoManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    private Config storageConfig;

    @Getter
    private Config rankConfig;

    @Getter
    private Config messageConfig;

    @Getter
    @Setter
    private MeleeMongoManager mongoManager;

    @Override
    public void onEnable() {
        instance = this;

        storageConfig = new Config("storage.yml");
        rankConfig = new Config("ranks.yml");
        messageConfig = new Config("messages.yml");

        new MeleeMongoManager(this);
        new MeleeRankManager(this);
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
