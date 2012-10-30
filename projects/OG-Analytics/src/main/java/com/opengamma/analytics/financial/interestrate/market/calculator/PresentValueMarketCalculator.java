/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.market.CouponFixedDiscountingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.market.CouponIborDiscountingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.market.CouponOISDiscountingMarketMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueMarketCalculator extends AbstractInstrumentDerivativeVisitor<IMarketBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueMarketCalculator INSTANCE = new PresentValueMarketCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueMarketCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueMarketCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CouponFixedDiscountingMarketMethod METHOD_CPN_FIXED = CouponFixedDiscountingMarketMethod.getInstance();
  private static final CouponIborDiscountingMarketMethod METHOD_CPN_IBOR = CouponIborDiscountingMarketMethod.getInstance();
  private static final CouponOISDiscountingMarketMethod METHOD_CPN_ON = CouponOISDiscountingMarketMethod.getInstance();

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment, final IMarketBundle market) {
    return METHOD_CPN_FIXED.presentValue(payment, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor payment, final IMarketBundle market) {
    return METHOD_CPN_IBOR.presentValue(payment, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponOIS payment, final IMarketBundle market) {
    return METHOD_CPN_ON.presentValue(payment, market);
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final IMarketBundle market) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(market, "Market");
    MultipleCurrencyAmount pv = visit(annuity.getNthPayment(0), market);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(visit(annuity.getNthPayment(loopp), market));
    }
    return pv;
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final IMarketBundle market) {
    return visitGenericAnnuity(annuity, market);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyAmount visitSwap(final Swap<?, ?> swap, final IMarketBundle market) {
    final MultipleCurrencyAmount pv1 = visit(swap.getFirstLeg(), market);
    final MultipleCurrencyAmount pv2 = visit(swap.getSecondLeg(), market);
    return pv1.plus(pv2);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final IMarketBundle market) {
    return visitSwap(swap, market);
  }

}
