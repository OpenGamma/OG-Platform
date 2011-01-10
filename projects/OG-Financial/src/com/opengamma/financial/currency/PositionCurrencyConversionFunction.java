/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.ComputationTargetType;

/**
 * Converts a value from one currency to another, acting on a position.
 */
public class PositionCurrencyConversionFunction extends CurrencyConversionFunction {

  public PositionCurrencyConversionFunction(final String valueName) {
    super(ComputationTargetType.POSITION, valueName);
  }

  public PositionCurrencyConversionFunction(final String... valueNames) {
    super(ComputationTargetType.POSITION, valueNames);
  }

}
