/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.method.DepositZeroDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the rate of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 */
public final class ParSpreadRateCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadRateCurveSensitivityCalculator INSTANCE = new ParSpreadRateCurveSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadRateCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadRateCurveSensitivityCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final PresentValueBasisPointCalculator PVBPC = PresentValueBasisPointCalculator.getInstance();
  private static final PresentValueBasisPointCurveSensitivityCalculator PVBPCSC = PresentValueBasisPointCurveSensitivityCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT_ZERO = DepositZeroDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureDiscountingMethod METHOD_IR_FUTURES = InterestRateFutureDiscountingMethod.getInstance();

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.parSpreadCurveSensitivity(deposit, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    ArgumentChecker.isTrue(swap.getFirstLeg().getCurrency().equals(swap.getSecondLeg().getCurrency()), "Both legs should have the same currency");
    InterestRateCurveSensitivity pvcs = new InterestRateCurveSensitivity(PVCSC.visit(swap, curves));
    InterestRateCurveSensitivity pvbpcs = PVBPCSC.visit(swap.getFirstLeg(), curves);
    double pvbp = PVBPC.visit(swap.getFirstLeg(), curves);
    double pv = PVC.visit(swap, curves);
    return pvcs.multiply(-1.0 / pvbp).plus(pvbpcs.multiply(pv / (pvbp * pvbp)));
  }

  @Override
  public InterestRateCurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(fra);
    return METHOD_FRA.parSpreadCurveSensitivity(fra, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return METHOD_IR_FUTURES.priceCurveSensitivity(future, curves);
  }

}
