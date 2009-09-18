/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * An implementation of {@link LiveDataAvailabilityProvider} where the
 * set of available {@link AnalyticValueDefinition}s is controlled externally
 * and fixed.
 *
 * @author kirk
 */
public class FixedLiveDataAvailabilityProvider implements LiveDataAvailabilityProvider {
  private final Set<AnalyticValueDefinition<?>> _availableDefinitions =
    new HashSet<AnalyticValueDefinition<?>>();

  @Override
  public synchronized boolean isAvailable(AnalyticValueDefinition<?> value) {
    return _availableDefinitions.contains(value);
  }
  
  public synchronized void addDefinition(AnalyticValueDefinition<?> value) {
    _availableDefinitions.add(value);
  }

}
