/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;

/**
 *
 */
public class InterestRateSwapSecurityUtils {

  public static InterestRateInstrumentType getSwapType(final InterestRateSwapSecurity security) {
    if (security.getLegs().size() != 2) {
      throw new IllegalArgumentException("InterestRateSwapSecurityUtils can only handle 2 legged swaps currently");
    }
    final InterestRateSwapLeg payLeg = security.getPayLeg();
    final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();

    if (payLeg.getNotional() instanceof InterestRateNotional && receiveLeg.getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payNotional = payLeg.getNotional();
      final InterestRateNotional receiveNotional = receiveLeg.getNotional();
      if (!payNotional.getCurrency().equals(receiveNotional.getCurrency())) {
        return InterestRateInstrumentType.SWAP_CROSS_CURRENCY;
      }
    }
    if (payLeg instanceof FixedInterestRateSwapLeg && receiveLeg instanceof FloatingInterestRateSwapLeg) {

      final FloatingInterestRateSwapLeg floatingLeg = (FloatingInterestRateSwapLeg) receiveLeg;
      //if (floatingLeg instanceof FloatingSpreadIRLeg) {
      //  return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
      //}
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
    } else if (payLeg instanceof FloatingInterestRateSwapLeg && receiveLeg instanceof FixedInterestRateSwapLeg) {
      final FloatingInterestRateSwapLeg floatingLeg = (FloatingInterestRateSwapLeg) payLeg;

      //if (floatingLeg instanceof FloatingSpreadIRLeg) {
      //  return InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD;
      //}
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
    if (payLeg instanceof FloatingInterestRateSwapLeg && receiveLeg instanceof FloatingInterestRateSwapLeg) {
      final FloatingInterestRateSwapLeg payLeg1 = (FloatingInterestRateSwapLeg) payLeg;
      final FloatingInterestRateSwapLeg receiveLeg1 = (FloatingInterestRateSwapLeg) receiveLeg;
      if (payLeg1.getFloatingRateType().isIbor()) {
        if (receiveLeg1.getFloatingRateType().isIbor()) {
          return InterestRateInstrumentType.SWAP_IBOR_IBOR;
        } else if (receiveLeg1.getFloatingRateType().isOis()) {
          return InterestRateInstrumentType.SWAP_IBOR_OIS;
        } else if (receiveLeg1.getFloatingRateType().isCms()) {
          return InterestRateInstrumentType.SWAP_IBOR_CMS;
        } else {
          throw new OpenGammaRuntimeException("Unknown swap type: " + security);
        }
      }
      if (receiveLeg1.getFloatingRateType().isIbor()) {
        if (payLeg1.getFloatingRateType().isOis()) {
          return InterestRateInstrumentType.SWAP_IBOR_OIS;
        } else if (payLeg1.getFloatingRateType().isCms()) {
          return InterestRateInstrumentType.SWAP_IBOR_CMS;
        } else {
          throw new OpenGammaRuntimeException("Unknown swap type: " + security);
        }
      }
      return InterestRateInstrumentType.SWAP_CMS_CMS;
    }
    throw new OpenGammaRuntimeException("Can only handle fixed-floating (pay and receive) swaps and floating-floating swaps, got " + security);
  }

  public static boolean payFixed(final InterestRateSwapSecurity security) {
    final InterestRateSwapLeg payLeg = security.getPayLeg();
    final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof FixedInterestRateSwapLeg && receiveLeg instanceof FloatingInterestRateSwapLeg) {
      return true;
    }
    if (payLeg instanceof FloatingInterestRateSwapLeg && receiveLeg instanceof FixedInterestRateSwapLeg) {
      return false;
    }
    throw new OpenGammaRuntimeException("Swap was not fixed / floating ");
  }

  //public static boolean isFloatFloat(final SwapSecurity security) {
  //  final SwapLegVisitor<Boolean> isFixed = new SwapLegVisitor<Boolean>() {
  //
  //    @Override
  //    public Boolean visitFixedInterestRateLeg(final com.opengamma.financial.security.swap.FixedInterestRateSwapLeg swapLeg) {
  //      return Boolean.TRUE;
  //    }
  //
  //    @Override
  //    public Boolean visitFloatingInterestRateLeg(final FloatingInterestRateSwapLeg swapLeg) {
  //      return Boolean.FALSE;
  //    }
  //
  //    @Override
  //    public Boolean visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
  //      return Boolean.FALSE;
  //    }
  //
  //    @Override
  //    public Boolean visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
  //      return Boolean.FALSE;
  //    }
  //
  //    @Override
  //    public Boolean visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
  //      return Boolean.TRUE;
  //    }
  //
  //    @Override
  //    public Boolean visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
  //      return Boolean.FALSE;
  //    }
  //
  //    @Override
  //    public Boolean visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
  //      return Boolean.TRUE;
  //    }
  //
  //    @Override
  //    public Boolean visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
  //      return Boolean.FALSE;
  //    }
  //
  //  };
  //  return !security.getPayLeg().accept(isFixed) && !security.getReceiveLeg().accept(isFixed);
  //}

}
