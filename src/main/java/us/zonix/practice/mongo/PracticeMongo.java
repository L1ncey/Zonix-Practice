package us.zonix.practice.mongo;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.Collections;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import us.zonix.practice.Practice;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;

public class PracticeMongo
{
    private static PracticeMongo instance;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> players;
    
    public PracticeMongo() {
        if (PracticeMongo.instance != null) {
            throw new RuntimeException("The mongo database has already been instantiated.");
        }
        PracticeMongo.instance = this;
        final FileConfiguration config = (FileConfiguration)Practice.getInstance().getMainConfig().getConfiguration();
        if (!config.contains("mongo.host") || !config.contains("mongo.port") || !config.contains("mongo.database") || !config.contains("mongo.authentication.enabled") || !config.contains("mongo.authentication.username") || !config.contains("mongo.authentication.password") || !config.contains("mongo.authentication.database")) {
            throw new RuntimeException("Missing configuration option");
        }
        if (config.getBoolean("mongo.authentication.enabled")) {
            final MongoCredential credential = MongoCredential.createCredential(config.getString("mongo.authentication.username"), config.getString("mongo.authentication.database"), config.getString("mongo.authentication.password").toCharArray());
            this.client = new MongoClient(new ServerAddress(config.getString("mongo.host"), config.getInt("mongo.port")), Collections.singletonList(credential));
        }
        else {
            this.client = new MongoClient(new ServerAddress(config.getString("mongo.host"), config.getInt("mongo.port")));
        }
        this.database = this.client.getDatabase(config.getString("mongo.database"));
        this.players = this.database.getCollection("players");
    }
    
    public MongoClient getClient() {
        return this.client;
    }
    
    public MongoDatabase getDatabase() {
        return this.database;
    }
    
    public MongoCollection<Document> getPlayers() {
        return this.players;
    }
    
    public static PracticeMongo getInstance() {
        return PracticeMongo.instance;
    }
}
