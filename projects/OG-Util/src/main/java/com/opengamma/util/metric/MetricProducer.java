/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.metric;

import com.yammer.metrics.MetricRegistry;

/**
 * An interface for any class capable of publishing Yammer metrics.
 */
public interface MetricProducer {

  /**
   * A call to tell the class to register its metrics with the given registry.
   * @param registry   the registry to register with
   * @param namePrefix the prefix for the actual metrics themselves, not null
   */
  void registerMetrics(MetricRegistry registry, String namePrefix);
}
