package xyz.kayaaa.xenon.bukkit.tools;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;

public final class TaskUtil {

    private static final Plugin plugin = XenonPlugin.getInstance();

    private TaskUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Runs a task synchronously.
     *
     * @param runnable The task to run.
     * @return The scheduled task.
     */
    public static BukkitTask runTask(Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Runs a task asynchronously.
     *
     * @param runnable The task to run.
     * @return The scheduled task.
     */
    public static BukkitTask runTaskAsynchronously(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Runs a task synchronously after a specified delay.
     *
     * @param runnable The task to run.
     * @param delay    The delay in ticks.
     * @return The scheduled task.
     */
    public static BukkitTask runTaskLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    /**
     * Runs a task asynchronously after a specified delay.
     *
     * @param runnable The task to run.
     * @param delay    The delay in ticks.
     * @return The scheduled task.
     */
    public static BukkitTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    /**
     * Runs a task synchronously at a fixed rate.
     *
     * @param runnable The task to run.
     * @param delay    The initial delay in ticks.
     * @param period   The period in ticks.
     * @return The scheduled task.
     */
    public static BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    /**
     * Runs a task asynchronously at a fixed rate.
     *
     * @param runnable The task to run.
     * @param delay    The initial delay in ticks.
     * @param period   The period in ticks.
     * @return The scheduled task.
     */
    public static BukkitTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    public static int runSyncTask(BukkitRunnable runnable, long delay, long period) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
    }

    /**
     * Cancels a scheduled task.
     *
     * @param task The task to cancel.
     */
    public static void cancelTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Cancels a scheduled task by its ID.
     *
     * @param taskId The ID of the task to cancel.
     */
    public static void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * Cancels all tasks scheduled by the plugin.
     */
    public static void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    public static boolean isRunning(BukkitRunnable task) {
        if (task == null) {
            return false; // Task is null, cannot check if running
        }
        try {
            int id = task.getTaskId();
            return Bukkit.getScheduler().isCurrentlyRunning(id) ||
                    Bukkit.getScheduler().isQueued(id);
        } catch (IllegalStateException e) {
            return false; // Task não foi agendada ainda
        }
    }
}
