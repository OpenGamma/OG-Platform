/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class SwapSecurityUtils {

  //TODO doesn't handle cross-currency swaps yet
  public static InterestRateInstrumentType getSwapType(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) receiveLeg;
      if (Double.doubleToLongBits(floatingLeg.getSpread()) == 0) {
        return InterestRateInstrumentType.SWAP_FIXED_IBOR;
      }
      return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) payLeg;
      if (Double.doubleToLongBits(floatingLeg.getSpread()) == 0) {
        return InterestRateInstrumentType.SWAP_FIXED_IBOR;
      }
      return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
    }
    if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return InterestRateInstrumentType.SWAP_IBOR_IBOR;
    }
    throw new OpenGammaRuntimeException(
        "Can only handle fixed-floating (pay and receive) swaps and floating-floating swaps");
  }

  public static boolean payFixed(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return true;
    }
    if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      return false;
    }
    throw new OpenGammaRuntimeException("Swap was not fixed / floating");
  }
}
