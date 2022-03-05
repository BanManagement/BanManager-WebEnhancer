package me.confuser.banmanager.webenhancer.bungee;

import org.bstats.bungeecord.Metrics;
import me.confuser.banmanager.webenhancer.common.CommonMetrics;

public class BungeeMetrics implements CommonMetrics {
  private final Metrics metrics;

  public BungeeMetrics(Metrics metrics) {
    this.metrics = metrics;
  }
}
