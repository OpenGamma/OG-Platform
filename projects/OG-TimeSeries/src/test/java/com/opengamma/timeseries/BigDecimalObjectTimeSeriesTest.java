/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.math.BigDecimal;

/**
 * Abstract test class with {@code BigDecimal} values.
 */
public abstract class BigDecimalObjectTimeSeriesTest<T> extends ObjectTimeSeriesTest<T, BigDecimal> {

  @Override
  protected BigDecimal[] testValues() {
    return new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4),
                              BigDecimal.valueOf(5), BigDecimal.valueOf(6) };
  }
    
  @Override
  protected BigDecimal[] emptyValues() {
    return new BigDecimal[] {};
  };

}
