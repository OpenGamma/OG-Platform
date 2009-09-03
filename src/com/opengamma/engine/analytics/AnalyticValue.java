/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

/**
 * Represents a particular value which is produced as a result of invoking
 * an {@link AnalyticFunction} over a set of inputs.
 *
 * @author kirk
 */
public interface AnalyticValue {
  AnalyticValueDefinition getDefinition();
  Object getValue();
}
