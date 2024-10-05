package gg.desolve.melee.rank;

import gg.desolve.melee.Melee;
import gg.desolve.melee.configuration.MeleeConfigManager;
import lombok.Data;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Rank {

    @Getter
    private static Map<String, Rank> ranks = new HashMap<>();

    private String name;
    private int priority = 1;
    private String display = name;
    private String prefix = "&f";
    private String color = "&f";
    private List<String> inherits = new ArrayList<>();
    private boolean grantable;
    private boolean visible;
    private boolean baseline; // default
    private List<String> permissions = new ArrayList<>();

    public Rank(String name) {
        this.name = name;
    }

    public Rank(String name, int priority, String display, String prefix, String color, List<String> inherits, boolean grantable, boolean visible, boolean baseline, List<String> permissions) {
        this.name = name;
        this.priority = priority;
        this.display = display;
        this.prefix = prefix;
        this.color = color;
        this.inherits = inherits;
        this.grantable = grantable;
        this.visible = visible;
        this.baseline = baseline;
        this.permissions = permissions;

        ranks.put(name, this);
    }

    public static Rank getRank(String name) {
        return ranks.values().stream()
                .filter(loopRank -> loopRank.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public String getNameColored() {
        return color + name;
    }

    public String getDisplayColored() {
        return color + display;
    }

    public static List<Rank> getSortedRanks() {
        return ranks.values().stream()
                .sorted(Comparator.comparingInt(Rank::getPriority).reversed())
                .collect(Collectors.toList());
    }

    public static boolean rankIsHigherThanRank(Rank rank, Rank compareRank) {
        return rank.getPriority() >= compareRank.getPriority();
    }

    public static Rank getDefault() {
        return ranks.values().stream()
                .filter(Rank::isBaseline)
                .findFirst()
                .orElse(null);
    }

    public List<String> getPermissionsAndInherited() {
        List<String> storePermissions = new ArrayList<>(permissions);
        for (String inheritedRank : inherits) {
            storePermissions.addAll(Rank.getRanks().get(inheritedRank).getPermissions());
        }

        return new ArrayList<>(storePermissions);
    }

    public static void load(String rank) {
        try {
            FileConfiguration config = MeleeConfigManager.getRankConfig().getConfig();

            new Rank(
                    rank,
                    config.getInt(rank + ".priority", 1),
                    config.getString(rank + ".display", rank),
                    config.getString(rank + ".prefix", "&f"),
                    config.getString(rank + ".color", "&f"),
                    config.getStringList(rank + ".inherits"),
                    config.getBoolean(rank + ".grantable", false),
                    config.getBoolean(rank + ".visible", false),
                    config.getBoolean(rank + ".baseline", false),
                    config.getStringList(rank + ".permissions")
            );

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving " + rank + " rank.");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            FileConfiguration config = MeleeConfigManager.getRankConfig().getConfig();

            config.set(name + ".priority", priority);
            config.set(name + ".name", display);
            config.set(name + ".prefix", prefix);
            config.set(name + ".color", color);
            config.set(name + ".inherits", inherits);
            config.set(name + "." + (baseline ? "baseline" : "grantable"), baseline || grantable);
            config.set(name + ".visible", visible);
            config.set(name + ".permissions", permissions);
            MeleeConfigManager.getRankConfig().save();

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving " + name + " rank.");
            e.printStackTrace();
        }
    }

}
