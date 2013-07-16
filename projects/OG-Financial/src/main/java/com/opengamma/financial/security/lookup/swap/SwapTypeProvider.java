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
public class SwapTypeProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public Object getValue(SwapSecurity security) {
    Pair<Currency, Currency> ccys = new CurrencyVisitor().visit(security);
    if (!ccys.getFirst().equals(ccys.getSecond())) {
      return "Cross Currency Swap";
    }
    FixedFloatVisitor visitor = new FixedFloatVisitor();
    if (!security.getPayLeg().accept(visitor) && !security.getReceiveLeg().accept(visitor)) {
      return "Basis Swap";
    }
    return "Interest Rate Swap";
  }
}
