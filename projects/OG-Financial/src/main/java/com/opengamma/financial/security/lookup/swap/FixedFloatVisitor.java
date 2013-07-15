/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;

/**
*
*/
/* package */ class FixedFloatVisitor implements SwapLegVisitor<Boolean> {

  @Override
  public Boolean visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
    return true;
  }

  @Override
  public Boolean visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
    return false;
  }

  @Override
  public Boolean visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
    return true;
  }

  @Override
  public Boolean visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
    return false;
  }
}
