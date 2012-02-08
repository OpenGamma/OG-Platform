/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
/**
 * Function to classify positions by Currency.
 *
 */
public class CurrenciesAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Currencies";
  private static final String NO_CURRENCY = "No currency";
  private final Comparator<Position> _comparator = new SimplePositionComparator();
  private SecuritySource _secSource;
  
  public CurrenciesAggregationFunction(SecuritySource secSource) {
    _secSource = secSource;
  }
  
  @Override
  public String classifyPosition(Position position) {
    try {
      Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(position.getSecurity(), _secSource);
      if (currencies == null || currencies.size() == 0) {
        return NO_CURRENCY;
      }
      switch (currencies.size()) {
        case 0:
          return NO_CURRENCY;
        case 1:
          return currencies.iterator().next().getCode();
        case 2: {
          Iterator<Currency> iter = currencies.iterator();
          Currency base = iter.next();
          Currency counter = iter.next();
          UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(base, counter);
          StringBuilder sb = new StringBuilder();
          sb.append(unordered.getFirstCurrency().getCode());
          sb.append("/");
          sb.append(unordered.getSecondCurrency().getCode());
          return sb.toString();
        }
        default: {
          StringBuilder sb = new StringBuilder();
          Iterator<Currency> iterator = currencies.iterator();
          while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
              sb.append("/");
            }
          }
          return sb.toString();
        }
      }
      
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

  @Override
  public int compare(String currency1, String currency2) {
    return currency1.compareTo(currency2);
  }
  
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
