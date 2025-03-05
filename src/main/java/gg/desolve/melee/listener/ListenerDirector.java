package gg.desolve.melee.listener;

import gg.desolve.melee.Melee;
import gg.desolve.melee.chat.ChatListener;
import gg.desolve.melee.profile.ProfileListener;
import lombok.Getter;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

@Getter
public class ListenerDirector {

    private final List<Listener> listeners;

    public ListenerDirector() {
        listeners = listeners();
    }

    private List<Listener> listeners() {
        List<Listener> listenerList = Arrays.asList(
                new ProfileListener(),
                new ChatListener()
        );

        listenerList.forEach(listener -> Melee.getInstance().getServer().getPluginManager().registerEvents(listener, Melee.getInstance()));
        return listenerList;
    }

}

