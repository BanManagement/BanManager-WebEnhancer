package me.confuser.banmanager.webenhancer.velocity;

import me.confuser.banmanager.webenhancer.common.CommonMetrics;
import org.bstats.velocity.Metrics;

public class VelocityMetrics implements CommonMetrics {
  private final Metrics metrics;

  public VelocityMetrics(Metrics metrics) {
    this.metrics = metrics;
  }
}
