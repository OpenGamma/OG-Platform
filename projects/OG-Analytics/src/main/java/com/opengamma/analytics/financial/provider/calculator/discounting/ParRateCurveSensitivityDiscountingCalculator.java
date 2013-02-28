/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Get the single fixed rate that makes the PV of the instrument zero.
 */
public final class ParRateCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateCurveSensitivityDiscountingCalculator INSTANCE = new ParRateCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParRateCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParRateCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the par rate of a swap with one fixed leg.
   * @param swap The Fixed coupon swap.
   * @param multicurves The multi-curves provider.
   * @return The par swap rate. If the fixed leg has been set up with some fixed payments these are ignored for the purposes of finding the swap rate
   */
  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, multicurves);
    final double pvbpBar = -pvSecond / (pvbp * pvbp);
    final double pvSecondBar = 1.0 / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy).multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity result = pvSecondDr.multipliedBy(pvSecondBar).plus(pvbpDr.multipliedBy(pvbpBar));
    return result;
  }

  /**
   * Computes the swap convention-modified par rate for a fixed coupon swap.
   * <P>Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   * @param swap The swap.
   * @param dayCount The day count convention to modify the swap rate.
   * @param multicurves The multi-curves provider.
   * @return The modified rate.
   */
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DayCount dayCount, final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, multicurves);
    final double pvbpBar = -pvSecond / (pvbp * pvbp);
    final double pvSecondBar = 1.0 / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy).multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity result = pvSecondDr.multipliedBy(pvSecondBar).plus(pvbpDr.multipliedBy(pvbpBar));
    return result;
  }

}
