/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class Strike implements StrikeType {

  private final double _value;

  public Strike(final double value) {
    Validate.isTrue(value >= 0, "negative strike");
    _value = value;
  }

  @Override
  public double value() {
    return _value;
  }


  @Override
  public Strike with(double value) {
    return new Strike(value);
  }

}
