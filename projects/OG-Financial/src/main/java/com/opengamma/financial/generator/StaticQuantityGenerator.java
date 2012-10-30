/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility class that always returns the same quantity amount for position/trade construction.
 */
public class StaticQuantityGenerator implements QuantityGenerator {

  private final BigDecimal _quantity;

  public StaticQuantityGenerator(final int quantity) {
    this(new BigDecimal(quantity));
  }

  public StaticQuantityGenerator(final long quantity) {
    this(new BigDecimal(quantity));
  }

  public StaticQuantityGenerator(final BigDecimal quantity) {
    ArgumentChecker.notNull(quantity, "quantity");
    _quantity = quantity;
  }

  @Override
  public BigDecimal createQuantity() {
    return _quantity;
  }

}
