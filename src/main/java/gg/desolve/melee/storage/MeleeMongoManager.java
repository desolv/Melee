package gg.desolve.melee.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import gg.desolve.melee.Melee;
import gg.desolve.melee.configuration.MeleeConfigManager;
import lombok.Getter;
import org.bson.UuidRepresentation;
import org.bukkit.plugin.Plugin;

@Getter
public class MeleeMongoManager {

    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;

    public MeleeMongoManager(Plugin plugin, long millis) {
        try {
            MeleeConfigManager configManager = new MeleeConfigManager(plugin);

            MongoClientSettings mongoSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(configManager.getStorage().getString("mongodb.url")))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();


            mongoClient = MongoClients.create(mongoSettings);
            mongoDatabase = mongoClient.getDatabase(configManager.getStorage().getString("mongodb.database"));

            Melee.getInstance().setMongoManager(this);
            plugin.getLogger().info("Merged MongoDB @ " + (System.currentTimeMillis() - millis) + "ms.");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem connecting to MongoDB.");
            e.printStackTrace();
        }
    }
}
