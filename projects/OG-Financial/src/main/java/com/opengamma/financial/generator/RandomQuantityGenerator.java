/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;
import java.util.Random;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility class that returns random quantities.
 */
public class RandomQuantityGenerator implements QuantityGenerator {

  private final Random _random = new Random();
  private final int _granularity;
  private final int _range;

  public RandomQuantityGenerator(final int granularity, final int range) {
    ArgumentChecker.isTrue(granularity > 0, "granularity");
    ArgumentChecker.isTrue(range > 0, "range");
    _granularity = granularity;
    _range = range;
  }

  protected int getGranularity() {
    return _granularity;
  }

  protected int getRange() {
    return _range;
  }

  protected Random getRandom() {
    return _random;
  }

  @Override
  public BigDecimal createQuantity() {
    return new BigDecimal((getRandom().nextInt(getRange()) + 1) * getGranularity());
  }
}
