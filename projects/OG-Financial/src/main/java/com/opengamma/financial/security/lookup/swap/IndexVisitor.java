/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class IndexVisitor extends MultiSwapLegVisitor<ExternalId> {

  @Override
  ExternalId visitFixedLeg(FixedInterestRateLeg leg) {
    return null;
  }

  @Override
  ExternalId visitFloatingPayLeg(FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId();
  }

  @Override
  ExternalId visitOtherLeg(FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId();
  }
}
