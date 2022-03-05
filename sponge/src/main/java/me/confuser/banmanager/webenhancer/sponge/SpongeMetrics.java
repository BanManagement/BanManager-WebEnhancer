package me.confuser.banmanager.webenhancer.sponge;

import org.bstats.sponge.Metrics;
import me.confuser.banmanager.webenhancer.common.CommonMetrics;

public class SpongeMetrics implements CommonMetrics {
  private final Metrics metrics;

  public SpongeMetrics(Metrics metrics) {
    this.metrics = metrics;
  }
}
