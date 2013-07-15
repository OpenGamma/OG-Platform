/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;

/**
 *
 */
public class FrequencyVisitor extends MultiSwapLegVisitor<Frequency> {

  @Override
  Frequency visitFixedLeg(final FixedInterestRateLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitOtherLeg(final FloatingInterestRateLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
    return leg.getFrequency();
  }
}
