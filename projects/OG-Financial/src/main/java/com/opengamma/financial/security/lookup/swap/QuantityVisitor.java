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

/**
 *
 */
public class QuantityVisitor extends MultiSwapLegVisitor<Double> {

  @Override
  Double visitFixedLeg(final FixedInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitOtherLeg(final FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }
}
