/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the sensitivity to the curves (in the Market description of curve bundle) of the market quote sensitivity.
 */
public final class PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator INSTANCE = new PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator() {
  }

  // -----     Payment/Coupon     ------

  @Override
  public MulticurveSensitivity visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurve) {
    return new MulticurveSensitivity();
  }

  public MulticurveSensitivity visitCoupon(final Coupon coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Market");
    ArgumentChecker.notNull(coupon, "Coupon");
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double mqsBar = 1.0;
    final double dfBar = coupon.getPaymentYearFraction() * coupon.getNotional() * mqsBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    //    resultMapDsc.put(coupon.getFundingCurveName(), listDiscounting);
    resultMapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    return MulticurveSensitivity.ofYieldDiscounting(resultMapDsc);
  }

  @Override
  public MulticurveSensitivity visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponONSpread(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponIbor(final CouponIbor coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponIborSpread(final CouponIborSpread coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponIborCompounding(final CouponIborCompounding coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public MulticurveSensitivity visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    // TODO: [PLAT-5978] Change to exact sensitivity.
    return visitCoupon(coupon, multicurve);
  }

  // -----     Annuity     ------

  @Override
  public MulticurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(annuity, "Annuity");
    MulticurveSensitivity pvbpSensi = new MulticurveSensitivity();
    for (final Payment p : annuity.getPayments()) {
      pvbpSensi = pvbpSensi.plus(p.accept(this, multicurve));
    }
    return pvbpSensi;
  }

  @Override
  public MulticurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

}
