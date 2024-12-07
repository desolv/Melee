package gg.desolve.melee.common;

import gg.desolve.melee.Melee;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Data
public class Schedule {

    private final transient Runnable runnable;
    private final String identity;
    private final long delay;
    private transient BukkitTask task;

    public Schedule(String identity, Runnable runnable, long delay) {
        this.identity = identity;
        this.runnable = runnable;
        this.delay = delay;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(
                Melee.getInstance(),
                runnable,
                delay / 50
        );
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

}