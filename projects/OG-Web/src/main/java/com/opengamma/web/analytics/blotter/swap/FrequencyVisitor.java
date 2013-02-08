/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;

/**
 *
 */
public class FrequencyVisitor extends MultiSwapLegVisitor<Frequency> {

  @Override
  Frequency visitFixedLeg(FixedInterestRateLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitFloatingPayLeg(FloatingInterestRateLeg leg) {
    return leg.getFrequency();
  }

  @Override
  Frequency visitOtherLeg(FloatingInterestRateLeg leg) {
    return leg.getFrequency();
  }
}
