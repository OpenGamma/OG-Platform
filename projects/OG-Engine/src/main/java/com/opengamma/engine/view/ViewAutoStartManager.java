/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Map;

import com.opengamma.engine.view.impl.AutoStartViewDefinition;

/**
 * Responsible for the handling of auto start views. Once the manager has
 * been initialized it will provide a list of views to be automatically
 * started. It is not responsible for actually starting the views nor fer
 * tracking which ones have been started.
 */
public interface ViewAutoStartManager {

  /**
   * Perform any initialization required.
   */
  void initialize();

  /**
   * Get the set of views that are configured to be automatically started.
   *
   * @return views eligible for automatic starting, not null
   */
  Map<String, AutoStartViewDefinition> getAutoStartViews();
}
