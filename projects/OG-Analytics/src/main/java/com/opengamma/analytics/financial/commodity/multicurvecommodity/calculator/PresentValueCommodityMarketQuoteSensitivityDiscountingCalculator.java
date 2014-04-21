/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.calculator;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the sensitivity to the curves (in the Market description of curve bundle) of the market quote sensitivity.
 */
public final class PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<CommodityProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator INSTANCE = new PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator() {
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final CommodityProviderInterface multicurve) {
    return 0.0;
  }

  public Double visitCoupon(final Payment coupon, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(coupon, "Coupon");
    return multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime()) * coupon.getReferenceAmount();
  }

  @Override
  public Double visitCouponFixed(final CouponFixed coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponONSpread(final CouponONSpread coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponIbor(final CouponIbor coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding coupon, final CommodityProviderInterface multicurve) {
    return visitCoupon(coupon, multicurve);
  }

  // -----     Annuity     ------

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Market");
    ArgumentChecker.notNull(annuity, "Annuity");
    double pvbp = 0;
    for (final Payment p : annuity.getPayments()) {
      pvbp += p.accept(this, multicurve);
    }
    return pvbp;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final CommodityProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    return swap.getFirstLeg().accept(this, multicurve);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final CommodityProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }
}
