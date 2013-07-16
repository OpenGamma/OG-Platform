/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;

/**
 *
 */
/* package */ class FixedFloatVisitor implements SwapLegVisitor<Boolean> {

  @Override
  public Boolean visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
    return true;
  }

  @Override
  public Boolean visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
    return true;
  }

  @Override
  public Boolean visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
    return true;
  }

  @Override
  public Boolean visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
    return false;
  }
}
