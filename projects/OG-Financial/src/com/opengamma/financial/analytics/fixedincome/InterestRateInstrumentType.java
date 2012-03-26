/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public enum InterestRateInstrumentType {
  /** A swap, one fixed leg, one floating referenced to an ibor rate */
  SWAP_FIXED_IBOR,
  /** A swap, one fixed leg, one floating referenced to an ibor rate and spread, paying fixed */
  SWAP_FIXED_IBOR_WITH_SPREAD,
  /** A swap, two floating legs referenced to ibor rates */
  SWAP_IBOR_IBOR,
  /** A swap, one fixed leg, one CMS leg */
  SWAP_FIXED_CMS,
  /** A swap, one ibor leg, one CMS leg */
  SWAP_IBOR_CMS,
  /** A swap, two CMS legs */
  SWAP_CMS_CMS,
  /** A swap, one fixed leg, one OIS leg */
  SWAP_FIXED_OIS,
  /** Cash */
  CASH, //TODO do we need ibor, deposit, OIS?
  /** FRA */
  FRA,
  /** Interest rate future */
  IR_FUTURE,
  /** Coupon bond */
  COUPON_BOND,
  /** Bond future */
  BOND_FUTURE;

  @SuppressWarnings("synthetic-access")
  private static final FinancialSecurityVisitor<InterestRateInstrumentType> TYPE_IDENTIFIER = new TypeIdentifier();

  public static InterestRateInstrumentType getInstrumentTypeFromSecurity(final FinancialSecurity security) {
    final InterestRateInstrumentType type = security.accept(TYPE_IDENTIFIER);
    if (type == null) {
      throw new OpenGammaRuntimeException("Can't handle " + security.getClass().getName());
    }
    return type;
  }

  public static boolean isFixedIncomeInstrumentType(final FinancialSecurity security) {
    try {
      return security.accept(TYPE_IDENTIFIER) != null;
    } catch (final OpenGammaRuntimeException e) {
      return false;
    }
  }

  private static class TypeIdentifier implements FinancialSecurityVisitor<InterestRateInstrumentType> {

    @Override
    public InterestRateInstrumentType visitBondSecurity(final BondSecurity security) {
      return COUPON_BOND;
    }

    @Override
    public InterestRateInstrumentType visitCashSecurity(final CashSecurity security) {
      return CASH;
    }

    @Override
    public InterestRateInstrumentType visitEquitySecurity(final EquitySecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitFRASecurity(final FRASecurity security) {
      return FRA;
    }

    @Override
    public InterestRateInstrumentType visitFutureSecurity(final FutureSecurity security) {
      if (security instanceof BondFutureSecurity) {
        return BOND_FUTURE;
      }
      if (security instanceof InterestRateFutureSecurity) {
        return IR_FUTURE;
      }
      return null;
    }

    @Override
    public InterestRateInstrumentType visitSwapSecurity(final SwapSecurity security) {
      return SwapSecurityUtils.getSwapType(security);
    }

    @Override
    public InterestRateInstrumentType visitSwaptionSecurity(final SwaptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitFXOptionSecurity(final FXOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitFXForwardSecurity(final FXForwardSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitCapFloorSecurity(final CapFloorSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
      return null;
    }

    @Override
    public InterestRateInstrumentType visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      return null;
    }
  }
}
