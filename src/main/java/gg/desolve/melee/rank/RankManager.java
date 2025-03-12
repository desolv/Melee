package gg.desolve.melee.rank;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.desolve.mithril.Mithril;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class RankManager {

    private final Map<String, Rank> records = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public RankManager() {
        deposit();
        primary();
    }

    public Rank retrieve(String rankName) {
        Rank rank = retrieve().stream()
                .filter(r -> r.getName().equalsIgnoreCase(rankName))
                .findFirst()
                .orElse(null);

        if (rank != null)
            return rank;

        rank = Mithril.getInstance().getMongoManager().getMongoDatabase()
                .getCollection("ranks")
                .find(Filters.eq("name", rankName))
                .map(rankDocument -> gson.fromJson(rankDocument.toJson(), Rank.class))
                .first();

        if (rank != null) {
            rank.setTimestamp(System.currentTimeMillis());
            records.put(rank.getName(), rank);
        }

        return rank;
    }

    public Rank create(String name) {
        return new Rank(
                name,
                1,
                name,
                "<white>",
                "<white>",
                new ArrayList<>(),
                true,
                true,
                false,
                new ArrayList<>()
        );
    }

    public void publish(Rank rank, String type) {
        Mithril.getInstance().getRedisManager().publish("Rank", rank.getName() + "&%$" + type);
    }

    public void save(Rank rank) {
        Mithril.getInstance().getMongoManager().getMongoDatabase().getCollection("ranks")
                .replaceOne(
                        Filters.eq("name", rank.getName()),
                        Document.parse(gson.toJson(rank)),
                        new ReplaceOptions().upsert(true));
    }

    public void delete(Rank rank) {
        Mithril.getInstance().getMongoManager().getMongoDatabase().getCollection("ranks")
                .deleteOne(Filters.eq("name", rank.getName()));
        publish(rank, "refresh");
    }

    public List<Rank> retrieve() {
        return records.values().stream().toList();
    }

    public void deposit() {
        records.clear();
        Mithril.getInstance().getMongoManager().getMongoDatabase()
                .getCollection("ranks")
                .find()
                .map(rankDocument -> gson.fromJson(rankDocument.toJson(), Rank.class))
                .into(new ArrayList<>())
               .forEach(rank -> {
                   rank.setTimestamp(System.currentTimeMillis());
                   records.put(rank.getName().toLowerCase(), rank);
               });
    }

    public List<Rank> sorted() {
        return retrieve().stream()
                .sorted(Comparator.comparingInt(Rank::getPriority).reversed())
                .collect(Collectors.toList());
    }

    public boolean compare(Rank rank, Rank compare) {
        return rank.getPriority() >= compare.getPriority();
    }

    public Rank primary() {
        Rank primary = retrieve().stream()
                .filter(Rank::isPrimary)
                .findFirst()
                .orElseGet(() -> create("Default"));

        primary.setPrimary(true);
        primary.setGrantable(false);
        primary.setVisible(true);
        save(primary);

        return primary;
    }
}
