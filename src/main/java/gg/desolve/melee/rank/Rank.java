package gg.desolve.melee.rank;

import gg.desolve.melee.Melee;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Rank {

    private String name;
    private int priority;
    private String display;
    private String prefix;
    private String color;
    private List<String> inherits;
    private boolean grantable;
    private boolean visible;
    private boolean primary;
    private List<String> permissions;
    private transient long timestamp;
    private transient boolean pending;

    public Rank(String name, int priority, String display, String prefix, String color, List<String> inherits, boolean grantable, boolean visible, boolean primary, List<String> permissions) {
        this.name = name;
        this.priority = priority;
        this.display = display;
        this.prefix = prefix;
        this.color = color;
        this.inherits = inherits;
        this.grantable = grantable;
        this.visible = visible;
        this.primary = primary;
        this.permissions = permissions;
    }

    public String getNameColored() {
        return color + name;
    }

    public String getDisplayColored() {
        return color + display;
    }

    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<>(this.permissions);
        RankManager rankManager = Melee.getInstance().getRankManager();

        inherits.forEach(inherit -> permissions.addAll(rankManager.retrieve(inherit).getPermissions()));

        return new ArrayList<>(permissions);
    }

    public boolean hasPermission(String permission) {
        List<String> permissions = getPermissions();

        return Arrays.stream(permission.split("\\|")).anyMatch(permissions::contains);
    }

    public void save() {
        RankManager rankManager = Melee.getInstance().getRankManager();
        rankManager.save(this);
        rankManager.publish(this, "/");
    }
}
