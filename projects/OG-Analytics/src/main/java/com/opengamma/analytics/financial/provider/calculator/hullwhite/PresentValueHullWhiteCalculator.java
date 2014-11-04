/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureTransactionHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.SwapFuturesPriceDeliverableTransactionHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponCMSHullWhiteApproximationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborHullWhiteApproximationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborHullWhiteMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueHullWhiteCalculator extends 
  InstrumentDerivativeVisitorDelegate<HullWhiteOneFactorProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueHullWhiteCalculator INSTANCE = new PresentValueHullWhiteCalculator();

  /**
   * Constructor.
   */
  private PresentValueHullWhiteCalculator() {
    super(new HullWhiteProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorIborHullWhiteMethod METHOD_CAPFLOOR_IBOR = CapFloorIborHullWhiteMethod.getInstance();
  private static final InterestRateFutureTransactionHullWhiteMethod METHOD_STIRFUT = InterestRateFutureTransactionHullWhiteMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionHullWhiteMethod METHOD_STIRFUT_OPT_MAR = InterestRateFutureOptionMarginTransactionHullWhiteMethod.getInstance();
  private static final SwapFuturesPriceDeliverableTransactionHullWhiteMethod METHOD_SWAPFUT = SwapFuturesPriceDeliverableTransactionHullWhiteMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_SWPT_PHYS = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();
  private static final SwaptionCashFixedIborHullWhiteApproximationMethod METHOD_SWPT_CASH = SwaptionCashFixedIborHullWhiteApproximationMethod.getInstance();
  private static final CouponCMSHullWhiteApproximationMethod METHOD_CMS_CPN = CouponCMSHullWhiteApproximationMethod.getInstance();

  //     -----     Payment/Coupon     -----

  @Override
  public MultipleCurrencyAmount visitCapFloorIbor(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_CAPFLOOR_IBOR.presentValue(cap, hullWhite);
  }

  @Override
  public MultipleCurrencyAmount visitCouponCMS(final CouponCMS cms, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_CMS_CPN.presentValue(cms, hullWhite);
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.presentValue(futures, hullWhite);
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT_OPT_MAR.presentValue(option, hullWhite);
  }

  @Override
  public MultipleCurrencyAmount visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAPFUT.presentValue(futures, hullWhite);
  }

  //     -----     Swaption     -----

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWPT_PHYS.presentValue(swaption, hullWhite);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWPT_CASH.presentValue(swaption, hullWhite);
  }

}
