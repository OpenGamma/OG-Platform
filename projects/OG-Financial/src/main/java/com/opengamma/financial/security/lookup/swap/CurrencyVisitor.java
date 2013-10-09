/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CurrencyVisitor extends MultiSwapLegVisitor<Currency> {

  @Override
  Currency visitFixedLeg(final FixedInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitOtherLeg(final FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }

  @Override
  Currency visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
    return leg.getNotional().accept(new NotionalCurrencyVisitor());
  }
}
