/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;

/**
 * 
 */
public enum SwapLegType {
  /**
   * Fixed interest rate leg type.
   */
  FIXED_INTEREST,
  /**
   * Floating interest rate leg type.
   */
  FLOATING_INTEREST,
  /**
   * Floating spread interest rate leg type.
   */
  FLOATING_SPREAD_INTEREST,
  /**
   * Floating gearing interest rate leg type.
   */
  FLOATING_GEARING_INTEREST,
  /**
   * Fixed variance swap leg type.
   */
  FIXED_VARIANCE,
  /**
   * Floating variance swap leg type.
   */
  FLOATING_VARIANCE,
  /**
   * Fixed inflation swap leg type.
   */
  FIXED_INFLATION,
  /**
   * Inflation index swap leg type.
   */
  INFLATION_INDEX;

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

      @Override
      public SwapLegType visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
        return FLOATING_SPREAD_INTEREST;
      }

      @Override
      public SwapLegType visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
        return FLOATING_GEARING_INTEREST;
      }

      @Override
      public SwapLegType visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
        return FIXED_VARIANCE;
      }

      @Override
      public SwapLegType visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
        return FLOATING_VARIANCE;
      }

      @Override
      public SwapLegType visitFixedInflationSwapLeg(FixedInflationSwapLeg swapLeg) {
        return FIXED_INFLATION;
      }

      @Override
      public SwapLegType visitInflationIndexSwapLeg(InflationIndexSwapLeg swapLeg) {
        return INFLATION_INDEX;
      }

    });
  }

  public <T> T accept(final SwapLegVisitor<T> visitor) {
    switch (this) {
      case FIXED_INTEREST:
        return visitor.visitFixedInterestRateLeg(null);
      case FLOATING_INTEREST:
        return visitor.visitFloatingInterestRateLeg(null);
      case FLOATING_SPREAD_INTEREST:
        return visitor.visitFloatingSpreadIRLeg(null);
      case FLOATING_GEARING_INTEREST:
        return visitor.visitFloatingGearingIRLeg(null);
      case FIXED_VARIANCE:
        return visitor.visitFixedVarianceSwapLeg(null);
      case FLOATING_VARIANCE:
        return visitor.visitFloatingVarianceSwapLeg(null);
      case FIXED_INFLATION:
        return visitor.visitFixedInflationSwapLeg(null);
      case INFLATION_INDEX:
        return visitor.visitInflationIndexSwapLeg(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapLegType: " + this);
    }
  }

}
