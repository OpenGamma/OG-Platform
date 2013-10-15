/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
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
  public Set<AutoStartViewDefinition> getAutoStartViews() {
    return ImmutableSet.of();
  }
}
