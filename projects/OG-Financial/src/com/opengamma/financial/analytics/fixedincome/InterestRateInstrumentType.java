/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
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

  private static final FinancialSecurityVisitor<InterestRateInstrumentType> TYPE_IDENTIFIER = new TypeIdentifier();

  public static InterestRateInstrumentType getInstrumentTypeFromSecurity(FinancialSecurity security) {
    return security.accept(TYPE_IDENTIFIER);
  }

  public static boolean isFixedIncomeInstrumentType(FinancialSecurity security) {
    try {
      security.accept(TYPE_IDENTIFIER);
      return true;
    } catch (OpenGammaRuntimeException e) {
      // a bit nasty but ensures consistency with the other method
      return false;
    }
  }

  private static class TypeIdentifier implements FinancialSecurityVisitor<InterestRateInstrumentType> {

    @Override
    public InterestRateInstrumentType visitBondSecurity(BondSecurity security) {
      return COUPON_BOND;
    }

    @Override
    public InterestRateInstrumentType visitCashSecurity(CashSecurity security) {
      return CASH;
    }

    @Override
    public InterestRateInstrumentType visitEquitySecurity(EquitySecurity security) {
      throw new OpenGammaRuntimeException("EquitySecurity is not an interest rate instrument");
    }

    @Override
    public InterestRateInstrumentType visitFRASecurity(FRASecurity security) {
      return FRA;
    }

    @Override
    public InterestRateInstrumentType visitFutureSecurity(FutureSecurity security) {
      if (security instanceof InterestRateFutureSecurity) {
        return IR_FUTURE;
      }
      throw new OpenGammaRuntimeException("Cannot handle this FutureSecurity");
    }

    @Override
    public InterestRateInstrumentType visitSwapSecurity(SwapSecurity security) {
      return SwapSecurityUtils.getSwapType(security);
    }

    @Override
    public InterestRateInstrumentType visitSwaptionSecurity(SwaptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this SwaptionSecurity");
    }

    @Override
    public InterestRateInstrumentType visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this EquityIndexOptionSecurity");
    }

    @Override
    public InterestRateInstrumentType visitEquityOptionSecurity(EquityOptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this EquityOptionSecurity");
    }

    @Override
    public InterestRateInstrumentType visitFXOptionSecurity(FXOptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this FXOptionSecurity");
    }

    @Override
    public InterestRateInstrumentType visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this IRFutureOptionSecurity");
    }

    @Override
    public InterestRateInstrumentType visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
      throw new OpenGammaRuntimeException("Cannot handle this FXBarrierOptionSecurity");
    }

  }
}
