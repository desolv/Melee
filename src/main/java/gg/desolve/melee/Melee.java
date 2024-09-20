package gg.desolve.melee;

import gg.desolve.melee.commands.MeleeCommandManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Override
    public void onEnable() {
        instance = this;

        new MeleeCommandManager(this);
    }

}
