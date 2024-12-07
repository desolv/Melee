package gg.desolve.melee.server;

import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Data
public class Reboot {

    private final UUID addedBy;
    private final long addedAt;
    private final Runnable runnable;
    private final long delay;
    private BukkitRunnable task;

    public Reboot(UUID addedBy, long addedAt, long delay) {
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.runnable = Bukkit::shutdown;
        this.delay = delay;
    }

    public void start() {
        List<Long> intervals = new ArrayList<>(Arrays.asList(
                TimeUnit.MINUTES.toMillis(30),
                TimeUnit.MINUTES.toMillis(10),
                TimeUnit.MINUTES.toMillis(5),
                TimeUnit.MINUTES.toMillis(2),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.SECONDS.toMillis(30),
                TimeUnit.SECONDS.toMillis(5),
                TimeUnit.SECONDS.toMillis(4),
                TimeUnit.SECONDS.toMillis(3),
                TimeUnit.SECONDS.toMillis(2),
                TimeUnit.SECONDS.toMillis(1)
        ));

        for (Iterator<Long> iterator = intervals.iterator(); iterator.hasNext(); ) {
            long interval = iterator.next();
            long scheduleTime = (addedAt + delay) - interval;

            if (scheduleTime > System.currentTimeMillis()) {
                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getOnlinePlayers().forEach(player ->
                                Message.send(player, "<red>Instance will be rebooting in " + Converter.millisToTime(interval) + "."));

                        if (interval == TimeUnit.SECONDS.toMillis(1))
                            runnable.run();
                    }
                };

                task.runTaskLater(Melee.getInstance(), (scheduleTime - System.currentTimeMillis()) / 50);
                iterator.remove();
            }
        }
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
