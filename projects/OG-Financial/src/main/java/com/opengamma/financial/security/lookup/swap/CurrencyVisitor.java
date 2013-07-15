/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CurrencyVisitor extends MultiSwapLegVisitor<Currency> {

  @Override
  Currency visitFixedLeg(FixedInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitFloatingPayLeg(FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitOtherLeg(FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }
}
