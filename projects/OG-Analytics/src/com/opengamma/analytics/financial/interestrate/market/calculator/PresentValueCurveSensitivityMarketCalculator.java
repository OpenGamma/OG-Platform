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
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
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

/**
 * Calculator of the present value curve sensitivity as multiple currency interest rate curve sensitivity.
 */
public final class PresentValueCurveSensitivityMarketCalculator extends AbstractInstrumentDerivativeVisitor<IMarketBundle, MultipleCurrencyCurveSensitivityMarket> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityMarketCalculator INSTANCE = new PresentValueCurveSensitivityMarketCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityMarketCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityMarketCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CouponFixedDiscountingMarketMethod METHOD_CPN_FIXED = CouponFixedDiscountingMarketMethod.getInstance();
  private static final CouponIborDiscountingMarketMethod METHOD_CPN_IBOR = CouponIborDiscountingMarketMethod.getInstance();
  private static final CouponOISDiscountingMarketMethod METHOD_CPN_ON = CouponOISDiscountingMarketMethod.getInstance();

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitCouponFixed(final CouponFixed payment, final IMarketBundle market) {
    return METHOD_CPN_FIXED.presentValueMarketSensitivity(payment, market);
  }

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitCouponIbor(final CouponIbor payment, final IMarketBundle market) {
    return METHOD_CPN_IBOR.presentValueMarketSensitivity(payment, market);
  }

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitCouponOIS(final CouponOIS payment, final IMarketBundle market) {
    return METHOD_CPN_ON.presentValueMarketSensitivity(payment, market);
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitGenericAnnuity(final Annuity<? extends Payment> annuity, final IMarketBundle market) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(market, "Market");
    MultipleCurrencyCurveSensitivityMarket cs = visit(annuity.getNthPayment(0), market);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(visit(annuity.getNthPayment(loopp), market));
    }
    return cs;
  }

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final IMarketBundle market) {
    return visitGenericAnnuity(annuity, market);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitSwap(final Swap<?, ?> swap, final IMarketBundle market) {
    final MultipleCurrencyCurveSensitivityMarket sensitivity1 = visit(swap.getFirstLeg(), market);
    final MultipleCurrencyCurveSensitivityMarket sensitivity2 = visit(swap.getSecondLeg(), market);
    return sensitivity1.plus(sensitivity2);
  }

  @Override
  public MultipleCurrencyCurveSensitivityMarket visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final IMarketBundle market) {
    return visitSwap(swap, market);
  }

}
