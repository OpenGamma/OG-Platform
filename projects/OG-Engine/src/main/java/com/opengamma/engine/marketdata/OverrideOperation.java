/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Applies an override operation that acts upon the original underlying data. This allows market data
 * to be injected that replaces the underlying data, or that shifts it (e.g. bump it 10%).
 */
public interface OverrideOperation {

  /**
   * Apply the override operation on the original object.
   *
   * @param requirement the value descriptor; so that information about the type of the value might be inferred
   * @param original the original market data value
   * @return the new market data value
   */
  Object apply(ValueRequirement requirement, Object original);

  // TODO: The requirement parameter is bad; this should really be a computation target

}
