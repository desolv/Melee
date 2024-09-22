package gg.desolve.melee;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import gg.desolve.melee.commands.MeleeCommandManager;
import gg.desolve.melee.commons.Configuration;
import gg.desolve.melee.database.MeleeMongoManager;
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
        new MeleeCommandManager(this);
    }


}
