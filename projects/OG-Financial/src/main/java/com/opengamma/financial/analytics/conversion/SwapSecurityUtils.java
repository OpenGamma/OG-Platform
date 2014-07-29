/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
public class SwapSecurityUtils {

  public static InterestRateInstrumentType getSwapType(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg.getNotional() instanceof InterestRateNotional && receiveLeg.getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payNotional = (InterestRateNotional) payLeg.getNotional();
      final InterestRateNotional receiveNotional = (InterestRateNotional) receiveLeg.getNotional();
      if (payLeg instanceof FixedInflationSwapLeg) {
        return InterestRateInstrumentType.ZERO_COUPON_INFLATION_SWAP;
      }
      if (payLeg instanceof InflationIndexSwapLeg) {
        return InterestRateInstrumentType.ZERO_COUPON_INFLATION_SWAP;
      }

      if (!payNotional.getCurrency().equals(receiveNotional.getCurrency())) {
        return InterestRateInstrumentType.SWAP_CROSS_CURRENCY;
      }
    }
    if (!payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region; have " + payLeg.getRegionId() + " and " + receiveLeg.getRegionId());
    }
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {

      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) receiveLeg;
      if (floatingLeg instanceof FloatingSpreadIRLeg) {
        return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
      }
      final FloatingRateType floatingRateType = floatingLeg.getFloatingRateType();
      switch (floatingRateType) {
        case IBOR:
          return InterestRateInstrumentType.SWAP_FIXED_IBOR;
        case CMS:
          return InterestRateInstrumentType.SWAP_FIXED_CMS;
        case OIS:
          return InterestRateInstrumentType.SWAP_FIXED_OIS;
        default:
          throw new OpenGammaRuntimeException("Unsupported Floating rate type: " + floatingRateType);
      }
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) payLeg;

      if (floatingLeg instanceof FloatingSpreadIRLeg) {
        return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
      }
      final FloatingRateType floatingRateType = floatingLeg.getFloatingRateType();
      switch (floatingRateType) {
        case IBOR:
          return InterestRateInstrumentType.SWAP_FIXED_IBOR;
        case CMS:
          return InterestRateInstrumentType.SWAP_FIXED_CMS;
        case OIS:
          return InterestRateInstrumentType.SWAP_FIXED_OIS;
        default:
          throw new OpenGammaRuntimeException("Unsupported Floating rate type: " + floatingRateType);
      }
    }
    if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg payLeg1 = (FloatingInterestRateLeg) payLeg;
      final FloatingInterestRateLeg receiveLeg1 = (FloatingInterestRateLeg) receiveLeg;
      if (payLeg1.getFloatingRateType().isIbor()) {
        if (receiveLeg1.getFloatingRateType().isIbor()) {
          return InterestRateInstrumentType.SWAP_IBOR_IBOR;
        }
        return InterestRateInstrumentType.SWAP_IBOR_CMS;
      }
      if (receiveLeg1.getFloatingRateType().isIbor()) {
        return InterestRateInstrumentType.SWAP_IBOR_CMS;
      }
      return InterestRateInstrumentType.SWAP_CMS_CMS;
    }
    throw new OpenGammaRuntimeException("Can only handle fixed-floating (pay and receive) swaps and floating-floating swaps");
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

  public static boolean isFloatFloat(final SwapSecurity security) {
    final SwapLegVisitor<Boolean> isFixed = new SwapLegVisitor<Boolean>() {

      @Override
      public Boolean visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return Boolean.TRUE;
      }

      @Override
      public Boolean visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return Boolean.TRUE;
      }

      @Override
      public Boolean visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        return Boolean.FALSE;
      }

      @Override
      public Boolean visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return Boolean.TRUE;
      }

      @Override
      public Boolean visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return Boolean.FALSE;
      }

    };
    return !security.getPayLeg().accept(isFixed) && !security.getReceiveLeg().accept(isFixed);
  }

}
