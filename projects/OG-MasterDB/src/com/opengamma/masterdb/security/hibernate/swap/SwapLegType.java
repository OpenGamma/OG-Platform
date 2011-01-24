/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;

/**
 * 
 */
public enum SwapLegType {
  /**
   * 
   */
  FIXED_INTEREST,
  /**
   * 
   */
  FLOATING_INTEREST;

  public static SwapLegType identify(final SwapLeg object) {
    return object.accept(new SwapLegVisitor<SwapLegType>() {

      @Override
      public SwapLegType visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
        return FIXED_INTEREST;
      }

      @Override
      public SwapLegType visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
        return FLOATING_INTEREST;
      }

    });
  }

  public <T> T accept(final SwapLegVisitor<T> visitor) {
    switch (this) {
      case FIXED_INTEREST:
        return visitor.visitFixedInterestRateLeg(null);
      case FLOATING_INTEREST:
        return visitor.visitFloatingInterestRateLeg(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapLegType: " + this);
    }
  }

}
