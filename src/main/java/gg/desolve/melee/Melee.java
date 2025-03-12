package gg.desolve.melee;

import gg.desolve.melee.command.CommandDirector;
import gg.desolve.melee.listener.ListenerDirector;
import gg.desolve.melee.profile.ProfileManager;
import gg.desolve.melee.rank.RankManager;
import gg.desolve.melee.subscribe.SubscriberDirector;
import gg.desolve.mithril.command.CommandManager;
import gg.desolve.mithril.config.Config;
import gg.desolve.mithril.config.ConfigurationManager;
import gg.desolve.mithril.relevance.Message;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    public ConfigurationManager configurationManager;

    @Getter
    public CommandManager commandManager;

    @Getter
    public CommandDirector commandDirector;

    @Getter
    public ProfileManager profileManager;

    @Getter
    public RankManager rankManager;

    @Getter
    public ListenerDirector listenerDirector;

    @Getter
    public SubscriberDirector subscriberDirector;

    @Override
    public void onEnable() {
        instance = this;

        configurationManager = new ConfigurationManager(this, "language.yml", "storage.yml");

        Message.setPrefix(getLanguageConfig().getString("server.prefix"));

        commandManager = new CommandManager(this, "melee.*");
        commandDirector = new CommandDirector(commandManager);

        profileManager = new ProfileManager();
        rankManager = new RankManager();

        listenerDirector = new ListenerDirector();
        subscriberDirector = new SubscriberDirector();
    }

    @Override
    public void onDisable() {
        profileManager.save();
    }

    public Config getLanguageConfig() {
        return configurationManager.getConfig("language.yml");
    }
}
