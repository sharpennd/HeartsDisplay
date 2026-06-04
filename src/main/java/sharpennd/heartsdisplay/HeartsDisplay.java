package sharpennd.heartsdisplay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class HeartsDisplay extends JavaPlugin implements Listener {

    private Scoreboard mainBoard;
    private Objective healthObjective;

    @Override
    public void onEnable() {
        mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Remove any existing objective with this name to avoid conflicts on reload
        Objective existing = mainBoard.getObjective("heartsDisplay");
        if (existing != null) {
            existing.unregister();
        }

        healthObjective = mainBoard.registerNewObjective("heartsDisplay", "health", ChatColor.RED + "\u2764");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        Bukkit.getPluginManager().registerEvents(this, this);

        // Apply to any players already online (e.g. after a reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mainBoard);
        }
    }

    @Override
    public void onDisable() {
        if (healthObjective != null) {
            healthObjective.unregister();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(mainBoard);
    }
}
