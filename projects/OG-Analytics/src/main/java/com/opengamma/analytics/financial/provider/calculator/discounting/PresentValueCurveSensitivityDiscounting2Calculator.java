/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculator of the present value curve sensitivity as multiple currency interest rate curve sensitivity.
 * This calculator contains "second best" approaches (hence the 2 in the name).
 * In general the methods used here would not be used in production.
 */
public final class PresentValueCurveSensitivityDiscounting2Calculator extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityDiscounting2Calculator INSTANCE = new PresentValueCurveSensitivityDiscounting2Calculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityDiscounting2Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityDiscounting2Calculator() {
  }

  /** The methods used by the different instruments. */
  private static final CouponONArithmeticAverageDiscountingMethod METHOD_CPN_AAON_EXACT = 
      CouponONArithmeticAverageDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadDiscountingMethod METHOD_CPN_AAON_SPREAD_EXACT = 
      CouponONArithmeticAverageSpreadDiscountingMethod.getInstance();

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, 
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON_EXACT.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, 
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON_SPREAD_EXACT.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurve, "multicurve");
    MultipleCurrencyMulticurveSensitivity cs = annuity.getNthPayment(0).accept(this, multicurve);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(annuity.getNthPayment(loopp).accept(this, multicurve));
    }
    return cs;
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final ParameterProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwap(final Swap<?, ?> swap, final ParameterProviderInterface multicurve) {
    final MultipleCurrencyMulticurveSensitivity sensitivity1 = swap.getFirstLeg().accept(this, multicurve);
    final MultipleCurrencyMulticurveSensitivity sensitivity2 = swap.getSecondLeg().accept(this, multicurve);
    return sensitivity1.plus(sensitivity2);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

}
