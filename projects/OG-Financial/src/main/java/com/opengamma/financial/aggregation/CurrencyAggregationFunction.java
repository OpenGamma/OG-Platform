/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
/**
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Currency";
  private static final String NO_CURRENCY = "No or multiple currencies";
  private final Comparator<Position> _comparator = new SimplePositionComparator();

  @Override
  public String classifyPosition(final Position position) {
    try {
      final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
      if (currency == null) {
        return NO_CURRENCY;
      }
      return currency.toString();
    } catch (final UnsupportedOperationException ex) {
      return NO_CURRENCY;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(final String currency1, final String currency2) {
    return currency1.compareTo(currency2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
