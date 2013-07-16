/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.lookup.SecurityValueProvider;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
*
*/
public class SwapProductProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public String getValue(SwapSecurity security) {
    Pair<Currency, Currency> currencies = new CurrencyVisitor().visit(security);
    // if the leg currencies are the same just use the code, if they're different use both codes with the
    // fixed currency first or the pay currency first for float/float swaps
    if (currencies.getFirst().equals(currencies.getSecond())) {
      return currencies.getFirst().getCode();
    } else {
      return currencies.getFirst() + "/" + currencies.getSecond();
    }
  }
}
