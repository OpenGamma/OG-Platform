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

  /**
   * The singleton instance.
   */
  private static MetricRegistry s_instance;

  /**
   * Creates an instance.
   */
  private OpenGammaMetricRegistry() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   * 
   * @return the registry, not null
   */
  public static MetricRegistry getInstance() {
    initializeIfNecessary();
    return s_instance;
  }

  /**
   * Sets the default registry.
   * This should only be set at startup.
   * 
   * @param metricRegistry  the registry, not null
   */
  public static synchronized void setRegistry(MetricRegistry metricRegistry) {
    ArgumentChecker.notNull(metricRegistry, "metricRegistry");
    s_instance = metricRegistry;
  }

  /**
   * Creates a basic registry.
   * 
   * @param registryName  the name, not null
   */
  public static synchronized void createBasicDebuggingRegistry(String registryName) {
    ArgumentChecker.notEmpty(registryName, "registryName");
    s_instance = new MetricRegistry(registryName);
    Slf4jReporter logReporter = Slf4jReporter.forRegistry(s_instance)
                                          .outputTo(LoggerFactory.getLogger(OpenGammaMetricRegistry.class))
                                          .convertRatesTo(TimeUnit.SECONDS)
                                          .convertDurationsTo(TimeUnit.MILLISECONDS)
                                          .build();
    logReporter.start(1, TimeUnit.MINUTES);
    JmxReporter jmxReporter = JmxReporter.forRegistry(s_instance).build();
    jmxReporter.start();
  }

  private static void initializeIfNecessary() {
    if (s_instance == null) {
      String throwAwayName = System.getProperty("user.name") + "-" + System.currentTimeMillis();
      createBasicDebuggingRegistry(throwAwayName);
    }
  }

}
