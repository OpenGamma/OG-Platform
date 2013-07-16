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
import com.opengamma.id.ExternalId;

/**
 *
 */
public class IndexVisitor extends MultiSwapLegVisitor<ExternalId> {

  @Override
  ExternalId visitFixedLeg(final FixedInterestRateLeg leg) {
    return null;
  }

  @Override
  ExternalId visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId();
  }

  @Override
  ExternalId visitOtherLeg(final FloatingInterestRateLeg leg) {
    return leg.getFloatingReferenceRateId();
  }

  @Override
  ExternalId visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
    return null;
  }

  @Override
  ExternalId visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
    return leg.getIndexId();
  }

  @Override
  ExternalId visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
    return leg.getIndexId();
  }
}
