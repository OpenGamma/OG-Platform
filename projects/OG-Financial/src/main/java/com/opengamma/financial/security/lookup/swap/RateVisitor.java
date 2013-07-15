/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;

/**
 *
 */
/* package */ class RateVisitor extends MultiSwapLegVisitor<Object> {

  @Override
  Double visitFixedLeg(FixedInterestRateLeg leg) {
    return leg.getRate();
  }

  @Override
  String visitFloatingPayLeg(FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId().getValue();
  }

  @Override
  Object visitOtherLeg(FloatingInterestRateLeg leg) {
    return null;
  }
}
