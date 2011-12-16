/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.core.position.Position;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
/**
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Currency";
  private static final String NO_CURRENCY = "No or multiple currencies";
  
  @Override
  public String classifyPosition(Position position) {
    try {
      Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
      if (currency == null) {
        return NO_CURRENCY;
      }
      return currency.toString();
    } catch (UnsupportedOperationException ex) {
      return NO_CURRENCY;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }
}
