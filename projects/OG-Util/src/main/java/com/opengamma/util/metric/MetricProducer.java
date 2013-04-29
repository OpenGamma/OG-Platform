/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.metric;

import com.codahale.metrics.MetricRegistry;

/**
 * An interface for any class capable of publishing Yammer metrics.
 */
public interface MetricProducer {

  /**
   * A call to tell the class to register its metrics with the given registry.
   * 
   * @param summaryRegistry   the registry to register summary statistics to, not null
   * @param detailRegistry    the registry to publish very detailed statistics to, not null
   * @param namePrefix  the prefix for the actual metrics themselves, not null
   */
  void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix);

}
