package sharpennd.heartsdisplay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.event.entity.EntityDamageEvent;

public final class HeartsDisplay extends JavaPlugin implements Listener {

    private Scoreboard mainBoard;
    private Objective healthObjective;

    @Override
    public void onEnable() {
        mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();

        Objective existing = mainBoard.getObjective("heartsDisplay");
        if (existing != null) {
            existing.unregister();
        }

        // Use "dummy" so the server never auto-sets scores for mobs or anything else.
        // We control every score update manually, players only.
        healthObjective = mainBoard.registerNewObjective("heartsDisplay", "dummy", ChatColor.RED + "\u2764");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mainBoard);
            updateScore(player);
        }
    }

    @Override
    public void onDisable() {
        if (healthObjective != null) {
            healthObjective.unregister();
        }
    }

    /**
     * Computes the heart count to display.
     * - Base max health is 20 HP = 10 hearts.
     * - We display current HP scaled to hearts (HP / 2), rounded down.
     * - Only shows above 10 if the player genuinely has more than 20 max HP
     *   (e.g. from health boost potions, golden apples, plugins granting extra hearts).
     */
    private void updateScore(Player player) {
        double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHp = player.getHealth();

        // Display raw HP value (20 = full health = 10 hearts).
        // This matches what players expect: 20 ❤ at full health, scales naturally
        // with bonus HP from golden apples, health boost effects, or plugins.
        int hp = (int) Math.floor(currentHp);

        healthObjective.getScore(player.getName()).setScore(hp);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setScoreboard(mainBoard);
        // Slight delay so the client is fully ready before we push the score
        Bukkit.getScheduler().runTaskLater(this, () -> updateScore(player), 2L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up the score entry when a player leaves
        if (healthObjective.getScore(event.getPlayer().getName()).isScoreSet()) {
            mainBoard.resetScores(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        // Run after the damage is applied so we read the updated HP
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) updateScore(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) updateScore(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        healthObjective.getScore(event.getEntity().getName()).setScore(0);
    }
}
