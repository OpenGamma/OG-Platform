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
  private OpenGammaMetricRegistry() {
  }
  
  private static MetricRegistry s_instance;
  
  public static MetricRegistry getInstance() {
    initializeIfNecessary();
    return s_instance;
  }
  
  public static synchronized void setRegistryName(String registryName) {
    ArgumentChecker.notEmpty(registryName, "registryName");
    s_instance = new MetricRegistry(registryName);
    // NOTE kirk 2013-04-22 -- The below is just while I'm working on
    // instrumentation. Needs a much better configuration form for this.
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
      setRegistryName(throwAwayName);
    }
  }

}
