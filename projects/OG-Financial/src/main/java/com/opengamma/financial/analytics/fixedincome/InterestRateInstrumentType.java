/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
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
  /** A cross-currency swap */
  SWAP_CROSS_CURRENCY,
  /** Cash */
  CASH, //TODO do we need ibor, deposit, OIS?
  /** Cashflow */
  CASHFLOW,
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

  private static final class TypeIdentifier extends FinancialSecurityVisitorSameValueAdapter<InterestRateInstrumentType> {

    private TypeIdentifier() {
      super(null);
    }

    @Override
    public InterestRateInstrumentType visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return COUPON_BOND;
    }

    @Override
    public InterestRateInstrumentType visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
      return COUPON_BOND;
    }

    @Override
    public InterestRateInstrumentType visitCorporateBondSecurity(final CorporateBondSecurity security) {
      return COUPON_BOND;
    }

    @Override
    public InterestRateInstrumentType visitCashSecurity(final CashSecurity security) {
      return CASH;
    }

    @Override
    public InterestRateInstrumentType visitCashFlowSecurity(final CashFlowSecurity security) {
      return CASHFLOW;
    }

    @Override
    public InterestRateInstrumentType visitFRASecurity(final FRASecurity security) {
      return FRA;
    }

    @Override
    public InterestRateInstrumentType visitBondFutureSecurity(final BondFutureSecurity security) {
      return BOND_FUTURE;
    }

    @Override
    public InterestRateInstrumentType visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return IR_FUTURE;
    }

    @Override
    public InterestRateInstrumentType visitSwapSecurity(final SwapSecurity security) {
      return SwapSecurityUtils.getSwapType(security);
    }

  }

  /**
   * Engine {@link ComputationTargetType} corresponding to securities which would return true for {@link #isFixedIncomeInstrumentType}.
   */
  public static final ComputationTargetType FIXED_INCOME_INSTRUMENT_TARGET_TYPE = FinancialSecurityTypes.CASH_SECURITY
      .or(FinancialSecurityTypes.FRA_SECURITY)
      .or(FinancialSecurityTypes.INTEREST_RATE_FUTURE_SECURITY)
      .or(FinancialSecurityTypes.SWAP_SECURITY);

}
