package sharpennd.playerhealth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class PlayerHealth extends JavaPlugin implements Listener {
   Plugin plugin;
   ScoreboardManager scoreboardManager;

   public void onEnable() {
      this.plugin = this;
      this.scoreboardManager = Bukkit.getScoreboardManager();
      Bukkit.getPluginManager().registerEvents(this, this.plugin);
   }

   public void startUpdatingScoreboard(Player player) {
      Bukkit.getScheduler().runTaskTimer(this, () -> {
         Scoreboard board = player.getScoreboard();
         Objective healthDisplay = board.getObjective("healthDisplay");
         if (healthDisplay == null) {
            healthDisplay = board.registerNewObjective("healthDisplay", Criteria.HEALTH);
            healthDisplay.setDisplaySlot(DisplaySlot.BELOW_NAME);
            healthDisplay.setDisplayName(ChatColor.RED + "❤");
         }

         Score score = healthDisplay.getScore(player.getName());
         score.setScore((int) player.getHealth());
      }, 0L, 20L);
   }

   public void createScoreboard(Player player) {
      Scoreboard board = this.scoreboardManager.getNewScoreboard();
      player.setScoreboard(board);
   }

   @EventHandler
   public void playerJoinEvent(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.createScoreboard(player);
      this.startUpdatingScoreboard(player);
   }

   public void onDisable() {
   }
}
