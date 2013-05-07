/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.metric;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains the {@link MetricRegistry} that should be used by OpenGamma components at runtime.
 * The component system will configure this at startup.
 */
public final class OpenGammaMetricRegistry {

  private static MetricRegistry s_summaryInstance;
  private static MetricRegistry s_detailedInstance;

  /**
   * Creates an instance.
   */
  private OpenGammaMetricRegistry() {
  }

  //-------------------------------------------------------------------------
  public static MetricRegistry getSummaryInstance() {
    initializeIfNecessary();
    return s_summaryInstance;
  }

  public static MetricRegistry getDetailedInstance() {
    initializeIfNecessary();
    return s_detailedInstance;
  }
  
  /**
   * Sets the summary registry.
   * This should only be set at startup.
   * 
   * @param metricRegistry  the registry, not null
   */
  public static synchronized void setSummaryRegistry(MetricRegistry metricRegistry) {
    ArgumentChecker.notNull(metricRegistry, "metricRegistry");
    s_summaryInstance = metricRegistry;
  }

  /**
   * Sets the detailed registry.
   * This should only be set at startup.
   * 
   * @param metricRegistry  the registry, not null
   */
  public static synchronized void setDetailedRegistry(MetricRegistry metricRegistry) {
    ArgumentChecker.notNull(metricRegistry, "metricRegistry");
    s_detailedInstance = metricRegistry;
  }

  /**
   * Creates a basic registry.
   */
  public static synchronized void createBasicDebuggingRegistry() {
    s_summaryInstance = new MetricRegistry();
    s_detailedInstance = s_summaryInstance;
    Slf4jReporter logReporter = Slf4jReporter.forRegistry(s_summaryInstance)
                                          .outputTo(LoggerFactory.getLogger(OpenGammaMetricRegistry.class))
                                          .convertRatesTo(TimeUnit.SECONDS)
                                          .convertDurationsTo(TimeUnit.MILLISECONDS)
                                          .build();
    logReporter.start(1, TimeUnit.MINUTES);
    JmxReporter jmxReporter = JmxReporter.forRegistry(s_summaryInstance).build();
    jmxReporter.start();
  }

  private static void initializeIfNecessary() {
    if (s_summaryInstance == null) {
      createBasicDebuggingRegistry();
    }
  }

}
