/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the sensitivity to the curves (in the Market description of curve bundle) of the market quote sensitivity.
 * @author marc
 */
public final class PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator extends AbstractInstrumentDerivativeVisitor<MulticurveProviderInterface, CurveSensitivityMarket> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator INSTANCE = new PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator() {
  }

  // -----     Payment/Coupon     ------

  @Override
  public CurveSensitivityMarket visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurve) {
    return new CurveSensitivityMarket();
  }

  public CurveSensitivityMarket visitCoupon(final Coupon coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Market");
    ArgumentChecker.notNull(coupon, "Coupon");
    double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    double mqsBar = 1.0;
    double dfBar = coupon.getPaymentYearFraction() * coupon.getNotional() * mqsBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    resultMapDsc.put(coupon.getFundingCurveName(), listDiscounting);
    return CurveSensitivityMarket.ofYieldDiscounting(resultMapDsc);
  }

  @Override
  public CurveSensitivityMarket visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public CurveSensitivityMarket visitCouponIbor(final CouponIbor coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public CurveSensitivityMarket visitCouponIborSpread(final CouponIborSpread coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public CurveSensitivityMarket visitCouponIborCompounded(final CouponIborCompounded coupon, final MulticurveProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  // -----     Annuity     ------

  @Override
  public CurveSensitivityMarket visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(annuity, "Annuity");
    CurveSensitivityMarket pvbpSensi = new CurveSensitivityMarket();
    for (final Payment p : annuity.getPayments()) {
      pvbpSensi = pvbpSensi.plus(visit(p, multicurve));
    }
    return pvbpSensi;
  }

  @Override
  public CurveSensitivityMarket visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

}
