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
/* package */ class RateVisitor extends MultiSwapLegVisitor<Object> {

  @Override
  Double visitFixedLeg(final FixedInterestRateLeg leg) {
    return leg.getRate();
  }

  @Override
  String visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId().getValue();
  }

  @Override
  Object visitOtherLeg(final FloatingInterestRateLeg leg) {
    return null;
  }

  @Override
  Double visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
    return leg.getRate();
  }

  @Override
  String visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
    return leg.getIndexId().getValue();
  }

  @Override
  Object visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
    return null;
  }
}
