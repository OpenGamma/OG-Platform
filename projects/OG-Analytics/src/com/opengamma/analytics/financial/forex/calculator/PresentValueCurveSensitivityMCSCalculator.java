/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;

/**
 * Calculator of the present value curve sensitivity for Forex derivatives.
 */
public class PresentValueCurveSensitivityMCSCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityMCSCalculator s_instance = new PresentValueCurveSensitivityMCSCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityMCSCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityMCSCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FXSWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCash(final Cash cash, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(cash.getCurrency(), METHOD_DEPOSIT.presentValueCurveSensitivity(cash, curves));
  }

  // -----     Coupon     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_IBOR.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_FIXED.presentValueCurveSensitivity(coupon, curves));
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    MultipleCurrencyInterestRateCurveSensitivity sensi = new MultipleCurrencyInterestRateCurveSensitivity();
    for (final Payment p : annuity.getPayments()) {
      sensi = sensi.plus(visit(p, data));
    }
    return sensi;
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    return METHOD_FXSWAP.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.presentValueCurveSensitivity(derivative, data);
  }

}
