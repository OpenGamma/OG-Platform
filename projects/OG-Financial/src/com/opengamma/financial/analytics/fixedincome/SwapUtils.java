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
public class SwapUtils {
  /**
   * Types of swaps
   */
  public enum SwapType {
    /** One fixed leg, one floating referenced to an ibor rate, paying fixed*/
    FIXED_IBOR_PAY_FIXED,
    /** One fixed leg, one floating referenced to an ibor rate, receiving fixed*/
    FIXED_IBOR_RECEIVE_FIXED,
    /** One fixed leg, one floating referenced to an ibor rate and spread, paying fixed*/
    IBOR_IBOR,
  }
  
  //TODO doesn't handle cross-currency swaps yet
  public static SwapType getSwapType(SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return SwapType.FIXED_IBOR_PAY_FIXED;
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      return SwapType.FIXED_IBOR_RECEIVE_FIXED;
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return SwapType.IBOR_IBOR;
    } else {
      throw new OpenGammaRuntimeException("Can only handle fixed-floating (pay and receive) swaps and floating-floating swaps");
    }
  }
}
