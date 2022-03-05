package me.confuser.banmanager.webenhancer.bukkit;

import org.bstats.bukkit.Metrics;
import me.confuser.banmanager.webenhancer.common.CommonMetrics;

public class BukkitMetrics implements CommonMetrics {
  private final Metrics metrics;

  public BukkitMetrics(Metrics metrics) {
    this.metrics = metrics;
  }
}
