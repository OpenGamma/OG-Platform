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
/**
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Currency";
  
  @Override
  public String classifyPosition(Position position) {
    try {
      return FinancialSecurityUtils.getCurrency(position.getSecurity()).toString();
    } catch (UnsupportedOperationException ex) {
      return "No or multiple currencies";
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
