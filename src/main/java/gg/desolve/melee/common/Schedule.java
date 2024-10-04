package gg.desolve.melee.common;

import gg.desolve.melee.Melee;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Data
public class Schedule {

    private final Runnable runnable;
    private final String identity;
    private final long millis;
    private BukkitTask task;

    public Schedule(String identity, Runnable runnable, long millis) {
        this.identity = identity;
        this.runnable = runnable;
        this.millis = millis;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(
                Melee.getInstance(),
                runnable,
                millis / 50
        );
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

}