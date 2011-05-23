/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public enum InterestRateInstrumentType {
  /** A swap, one fixed leg, one floating referenced to an ibor rate */
  SWAP_FIXED_IBOR,
  /** A swap, one fixed leg, one floating referenced to an ibor rate and spread, paying fixed */
  SWAP_IBOR_IBOR,
  /** Cash */
  CASH, //TODO do we need ibor, deposit, OIS?
  /** FRA */
  FRA,
  /** Interest rate future */
  IR_FUTURE,
  /** Coupon bond */
  COUPON_BOND;

  public static InterestRateInstrumentType getInstrumentTypeFromSecurity(Security security) {
    if (security instanceof CashSecurity) {
      return CASH;
    }
    if (security instanceof FRASecurity) {
      return FRA;
    }
    if (security instanceof InterestRateFutureSecurity) {
      return IR_FUTURE;
    }
    if (security instanceof BondSecurity) {
      return COUPON_BOND;
    }
    if (security instanceof SwapSecurity) {
      return SwapSecurityUtils.getSwapType((SwapSecurity) security);
    }
    throw new OpenGammaRuntimeException("Cannot handle this security");
  }

  public static boolean isFixedIncomeInstrumentType(Security security) {
    if (security instanceof CashSecurity) {
      return true;
    }
    if (security instanceof FRASecurity) {
      return true;
    }
    if (security instanceof InterestRateFutureSecurity) {
      return true;
    }
    if (security instanceof BondSecurity) {
      return true;
    }
    if (security instanceof SwapSecurity) {
      SwapSecurityUtils.getSwapType((SwapSecurity) security);
      return true;
    }
    return false;
  }
}
