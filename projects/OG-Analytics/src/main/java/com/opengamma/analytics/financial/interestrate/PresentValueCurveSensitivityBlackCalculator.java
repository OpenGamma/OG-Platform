/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value curve sensitivity calculator for interest rate instruments using the Black formula.
 * @deprecated Use {@link PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator}
 */
@Deprecated
public final class PresentValueCurveSensitivityBlackCalculator extends PresentValueCurveSensitivityCalculator {

  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityBlackCalculator INSTANCE = new PresentValueCurveSensitivityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod PHYSICAL_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod CASH_SWAPTION = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod METHOD_SWAPTION_PHYSICAL_COMPOUNDED = SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance();

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return CASH_SWAPTION.presentValueCurveSensitivity(swaption, curvesBlack).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivityBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_SWAPTION.presentValueCurveSensitivity(swaption, curvesBlack).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curveBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL_COMPOUNDED.presentValueCurveSensitivity(swaption, curveBlack).getSensitivities();
    }
    throw new UnsupportedOperationException(
        "The PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedCompoundedONCompounded requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    return MARGINED_IR_FUTURE_OPTION.presentValueCurveSensitivity(transaction, curves).getSensitivities();
  }

  //TODO check this
  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    final InterestRateFutureOptionPremiumSecurity premiumUnderlying = transaction.getUnderlyingOption();
    final InterestRateFutureOptionMarginSecurity underlyingOption = new InterestRateFutureOptionMarginSecurity(premiumUnderlying.getUnderlyingFuture(),
        premiumUnderlying.getExpirationTime(), premiumUnderlying.getStrike(), premiumUnderlying.isCall());
    final InterestRateFutureOptionMarginTransaction marginTransaction = new InterestRateFutureOptionMarginTransaction(underlyingOption, transaction.getQuantity(), transaction.getTradePrice());
    return MARGINED_IR_FUTURE_OPTION.presentValueCurveSensitivity(marginTransaction, curves).getSensitivities();
  }

  //TODO check this
  @Override
  public Map<String, List<DoublesPair>> visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    return PREMIUM_BOND_FUTURE_OPTION.presentValueCurveSensitivity(transaction, curves).getSensitivities();
  }
}
