/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<Currency> {
  private static final String NAME = "Currency";
  
  @Override
  public Currency classifyPosition(Position position) {
    return FinancialSecurityUtils.getCurrency(position.getSecurity());
  }

  public String getName() {
    return NAME;
  }
}
