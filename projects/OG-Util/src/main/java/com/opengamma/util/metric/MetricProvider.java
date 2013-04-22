/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.metric;

import com.yammer.metrics.MetricRegistry;

/**
 * 
 */
public interface MetricProvider {

  void registerMetrics(MetricRegistry registry, String namePrefix);
}
