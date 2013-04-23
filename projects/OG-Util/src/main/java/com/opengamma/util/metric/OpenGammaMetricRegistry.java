/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.metric;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.yammer.metrics.JmxReporter;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.Slf4jReporter;

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
   * 
   * @param registryName  the name, not null
   */
  public static synchronized void createBasicDebuggingRegistry(String registryName) {
    ArgumentChecker.notEmpty(registryName, "registryName");
    s_summaryInstance = new MetricRegistry(registryName);
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
      String throwAwayName = System.getProperty("user.name") + "-" + System.currentTimeMillis();
      createBasicDebuggingRegistry(throwAwayName);
    }
  }

}
