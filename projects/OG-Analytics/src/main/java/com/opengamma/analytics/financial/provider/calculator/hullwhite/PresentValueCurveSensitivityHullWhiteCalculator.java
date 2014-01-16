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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the sensitivity of the present value to curves using the Hull-White one factor model.
 */
public final class PresentValueCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorDelegate<HullWhiteOneFactorProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteCalculator INSTANCE = new PresentValueCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityHullWhiteCalculator() {
    super(new HullWhiteProviderAdapter<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance()));
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
  public MultipleCurrencyMulticurveSensitivity visitCapFloorIbor(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_CAPFLOOR_IBOR.presentValueCurveSensitivity(cap, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponCMS(final CouponCMS cms, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_CMS_CPN.presentValueCurveSensitivity(cms, hullWhite);
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.presentValueCurveSensitivity(future, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option,
      final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT_OPT_MAR.presentValueCurveSensitivity(option, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAPFUT.presentValueCurveSensitivity(futures, hullWhite);
  }

  //     -----     Swaption     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWPT_PHYS.presentValueCurveSensitivity(swaption, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWPT_CASH.presentValueCurveSensitivity(swaption, hullWhite);
  }

}
