/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Set;

/**
 * A management bean for a ViewProcessor
 *
 */
public interface ViewProcessorMBean {
  
  /**
   * Gets the names of the views to which the view processor can provide access. Not all of these views are necessarily
   * initialized, less so being processed.
   * 
   * @return a set of view names
   */
  Set<String> getViewNames();
  
  /**
   * Start this ViewProcessor
   */
  void start();

  /**
   * Stop this ViewProcessor.
   * 
   */
  void stop();

  /**
   * Check whether this ViewProcessor is currently running.
   * @return whether the component is currently running
   */
  boolean isRunning();
  
}
