package gg.desolve.melee.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import gg.desolve.melee.Melee;
import lombok.Getter;
import org.bson.UuidRepresentation;

@Getter
public class MeleeMongoManager {

    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;

    public MeleeMongoManager(Melee instance) {
        try {
            MongoClientSettings mongoSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(instance.getStorageConfig().getString("mongodb.url")))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();

            mongoClient = MongoClients.create(mongoSettings);
            mongoDatabase = mongoClient.getDatabase(instance.getStorageConfig().getString("mongodb.database"));

            Melee.getInstance().setMongoManager(this);
            instance.getLogger().info("Connected to MongoDB.");
        } catch (Exception e) {
            instance.getLogger().warning("There was a problem connecting to MongoDB.");
            e.printStackTrace();
        }
    }
}
