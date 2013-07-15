/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;

/**
 *
 */
public class QuantityVisitor extends MultiSwapLegVisitor<Double> {

  @Override
  Double visitFixedLeg(FixedInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitFloatingPayLeg(FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }

  @Override
  Double visitOtherLeg(FloatingInterestRateLeg leg) {
    return leg.getNotional().accept(new NotionalAmountVisitor());
  }
}
