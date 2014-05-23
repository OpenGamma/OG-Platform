/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.calculator;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.CommodityFutureTransactionForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.CouponCommodityCashSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.CouponCommodityPhysicalSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.ForwardCommodityCashSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.ForwardCommodityPhysicalSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCommodityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<CommodityProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCommodityDiscountingCalculator INSTANCE = new PresentValueCommodityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCommodityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCommodityDiscountingCalculator() {
  }

  private static final CommodityFutureTransactionForwardMethod METHOD_COMMODITY_FUTURE = CommodityFutureTransactionForwardMethod.getInstance();
  private static final CouponCommodityCashSettleSecurityForwardMethod METHOD_COUPON_COMMODITY_CASH_COUPON = CouponCommodityCashSettleSecurityForwardMethod.getInstance();
  private static final CouponCommodityPhysicalSettleSecurityForwardMethod METHOD_COUPON_COMMODITY_PHYSICAL_COUPON = CouponCommodityPhysicalSettleSecurityForwardMethod.getInstance();
  private static final ForwardCommodityCashSettleSecurityForwardMethod METHOD_FWD_COMMODITY_CASH_COUPON = ForwardCommodityCashSettleSecurityForwardMethod.getInstance();
  private static final ForwardCommodityPhysicalSettleSecurityForwardMethod METHOD_FWD_COMMODITY_PHYSICAL_COUPON = ForwardCommodityPhysicalSettleSecurityForwardMethod.getInstance();

  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();

  //-----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitCouponCommodityCashSettle(final CouponCommodityCashSettle payment, final CommodityProviderInterface multicurve) {
    return METHOD_COUPON_COMMODITY_CASH_COUPON.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle payment, final CommodityProviderInterface multicurve) {
    return METHOD_COUPON_COMMODITY_PHYSICAL_COUPON.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitForwardCommodityCashSettle(final ForwardCommodityCashSettle payment, final CommodityProviderInterface multicurve) {
    return METHOD_FWD_COMMODITY_CASH_COUPON.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle payment, final CommodityProviderInterface multicurve) {
    return METHOD_FWD_COMMODITY_PHYSICAL_COUPON.presentValue(payment, multicurve);
  }

  //-----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurve, "multicurve");
    MultipleCurrencyAmount pv = annuity.getNthPayment(0).accept(this, multicurve);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(annuity.getNthPayment(loopp).accept(this, multicurve));
    }
    return pv;
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final CommodityProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyAmount visitSwap(final Swap<?, ?> swap, final CommodityProviderInterface multicurve) {
    final MultipleCurrencyAmount pv1 = swap.getFirstLeg().accept(this, multicurve);
    final MultipleCurrencyAmount pv2 = swap.getSecondLeg().accept(this, multicurve);
    return pv1.plus(pv2);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final CommodityProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitSwapMultileg(final SwapMultileg swap, final CommodityProviderInterface multicurve) {
    final int nbLegs = swap.getLegs().length;
    MultipleCurrencyAmount pv = swap.getLegs()[0].accept(this, multicurve);
    for (int loopleg = 1; loopleg < nbLegs; loopleg++) {
      pv = pv.plus(swap.getLegs()[loopleg].accept(this, multicurve));
    }
    return pv;
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitAgricultureFutureTransaction(final AgricultureFutureTransaction futures, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.presentValue(futures, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitEnergyFutureTransaction(final EnergyFutureTransaction futures, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.presentValue(futures, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitMetalFutureTransaction(final MetalFutureTransaction futures, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.presentValue(futures, multicurves);
  }

}
