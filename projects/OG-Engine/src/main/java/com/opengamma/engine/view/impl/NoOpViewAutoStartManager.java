/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.view.ViewAutoStartManager;

/**
 * Implementation of the ViewAutoStartManager that ignores
 * any configured auto-start views and just returns an empty
 * set. Used for cases where having views automatically
 * started is not required or desired.
 */
public class NoOpViewAutoStartManager implements ViewAutoStartManager {

  @Override
  public void initialize() {
    // Nothing to start
  }

  @Override
  public Map<String, AutoStartViewDefinition> getAutoStartViews() {
    return ImmutableMap.of();
  }
}
