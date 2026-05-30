package me.confuser.banmanager.webenhancer.common.listeners;

import me.confuser.banmanager.api.event.EventBus;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;
import me.confuser.banmanager.api.event.player.PlayerDeniedEvent;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Subscribes to BanManager's typed event bus and injects the {@code <pin>}
 * placeholder into kick / deny messages so operators can recover access via
 * the web UI.
 *
 * <p>BanManager applies any entries added to {@code event.placeholders()} to
 * the kick message template before rendering, so this listener never needs
 * to touch BanManager's internal {@code Message} class.</p>
 */
public class CommonPlayerDeniedListener {
  private final WebEnhancerPlugin plugin;

  public CommonPlayerDeniedListener(WebEnhancerPlugin plugin) {
    this.plugin = plugin;
  }

  public void register() {
    EventBus events = plugin.getBanManagerService().events();
    events.subscribe(PlayerDeniedEvent.class, this::onDenied);
    events.subscribe(PlayerBannedEvent.class, this::onBanned);
  }

  private void onDenied(PlayerDeniedEvent event) {
    if (event.uuid().isEmpty()) return;
    PlayerData player = lookupPlayer(event.uuid().get());
    if (player == null) return;
    injectPin(player, event.placeholders());
  }

  private void onBanned(PlayerBannedEvent event) {
    PlayerData player = lookupPlayer(event.ban().player().uuid());
    if (player == null) return;
    injectPin(player, event.placeholders());
  }

  private PlayerData lookupPlayer(UUID uuid) {
    try {
      return plugin.getBanManagerPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to look up player " + uuid + " for pin injection: " + e.getMessage());
      return null;
    }
  }

  private void injectPin(PlayerData player, Map<String, String> placeholders) {
    PlayerPinData pin = plugin.getPlayerPinStorage().getValidPin(player);
    if (pin != null) {
      placeholders.put("pin", String.valueOf(pin.getGeneratedPin()));
    }
  }
}
