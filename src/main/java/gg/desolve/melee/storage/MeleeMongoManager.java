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

    public MeleeMongoManager(Plugin plugin) {
        try {
            MongoClientSettings mongoSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(MeleeConfigManager.getStorageConfig().getString("mongodb.url")))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();


            mongoClient = MongoClients.create(mongoSettings);
            mongoDatabase = mongoClient.getDatabase(MeleeConfigManager.getStorageConfig().getString("mongodb.database"));

            Melee.getInstance().setMongoManager(this);
            plugin.getLogger().info("Connected to MongoDB.");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem connecting to MongoDB.");
            e.printStackTrace();
        }
    }
}
