/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value calculator for interest rate instruments using Black model with implied volatilities.
 * @deprecated Use the present values calculators that reference {@link ParameterProviderInterface}
 * e.g. {@link PresentValueBlackSwaptionCalculator}
 */
@Deprecated
public final class PresentValueBlackCalculator extends PresentValueCalculator {

  /**
   * The method unique instance.
   */
  private static final PresentValueBlackCalculator INSTANCE = new PresentValueBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackCalculator() {
  }

  /** Physical fixed / ibor swaption calculator */
  private static final SwaptionPhysicalFixedIborBlackMethod PHYSICAL_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /** Cash fixed / ibor swaption calculator */
  private static final SwaptionCashFixedIborBlackMethod CASH_SWAPTION = SwaptionCashFixedIborBlackMethod.getInstance();
  /** Margined interest rate future option calculator */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  /** Margined interest rate future option calculator */
  private static final InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  /** Bond future option with premium calculator */
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  /** Physical fixed accrued / overnight swaption calculator */
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod PHYSICAL_COMPOUNDED_SWAPTION = SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance();
  /** Cash fixed accrued / overnight swaption calculator */
  private static final SwaptionCashFixedCompoundedONCompoundedBlackMethod CASH_COMPOUNDED_SWAPTION = SwaptionCashFixedCompoundedONCompoundedBlackMethod.getInstance();

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return CASH_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_COMPOUNDED_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return CASH_COMPOUNDED_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return MARGINED_IR_FUTURE_OPTION.presentValue(option, curves).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

   @Override
   public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
     ArgumentChecker.notNull(curves, "curves");
     ArgumentChecker.notNull(option, "option");
     if (curves instanceof YieldCurveWithBlackCubeBundle) {
       return PREMIUM_IR_FUTURE_OPTION.presentValue(option, curves).getAmount();
     }
     throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitInterestRateFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
   }
 

  @Override
  public Double visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    return PREMIUM_BOND_FUTURE_OPTION.presentValue(option, curves).getAmount();
  }

}
