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
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
/**
 * Function to classify positions by Currency.
 *
 */
public class CurrenciesAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Currencies";
  /**
   * Name for No currency
   */
  public static final String NO_CURRENCY = "No currency";
  private final Comparator<Position> _comparator = new SimplePositionComparator();
  private final SecuritySource _secSource;

  public CurrenciesAggregationFunction(final SecuritySource secSource) {
    _secSource = secSource;
  }

  /**
   * Gets the security source.
   * @return The security source
   */
  protected SecuritySource getSecuritySource() {
    return _secSource;
  }

  @Override
  public String classifyPosition(final Position position) {
    return classifyBasedOnSecurity(position.getSecurity(), _secSource);
  }

  public static String classifyBasedOnSecurity(final Security security, final SecuritySource securitySource) {
    try {
      final Collection<Currency> currencies = CurrenciesVisitor.getCurrencies(security, securitySource);
      if (currencies == null || currencies.size() == 0) {
        return NO_CURRENCY;
      }
      switch (currencies.size()) {
        case 0:
          return NO_CURRENCY;
        case 1:
          return currencies.iterator().next().getCode();
        case 2: {
          final Iterator<Currency> iter = currencies.iterator();
          final Currency base = iter.next();
          final Currency counter = iter.next();
          final UnorderedCurrencyPair unordered = UnorderedCurrencyPair.of(base, counter);
          final StringBuilder sb = new StringBuilder();
          sb.append(unordered.getFirstCurrency().getCode());
          sb.append("/");
          sb.append(unordered.getSecondCurrency().getCode());
          return sb.toString();
        }
        default: {
          final StringBuilder sb = new StringBuilder();
          final Iterator<Currency> iterator = currencies.iterator();
          while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
              sb.append("/");
            }
          }
          return sb.toString();
        }
      }

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
