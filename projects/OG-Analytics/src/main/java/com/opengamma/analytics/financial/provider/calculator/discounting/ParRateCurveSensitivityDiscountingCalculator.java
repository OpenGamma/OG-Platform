/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Get the single fixed rate that makes the PV of the instrument zero.
 */
public final class ParRateCurveSensitivityDiscountingCalculator 
  extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MulticurveSensitivity> {

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
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, multicurves.getMulticurveProvider());
    final double pvbpBar = -pvSecond / (pvbp * pvbp);
    final double pvSecondBar = 1.0 / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, multicurves.getMulticurveProvider());
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

  /**
   * Computes the swap convention-modified par rate curve sensitivity for a fixed coupon swap.
   * <P>Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   * @param swap The swap.
   * @param dayCount The day count convention to modify the swap rate.
   * @param multicurves The multi-curves provider.
   * @return The modified rate curve sensitivity.
   */
  public MulticurveSensitivity visitFixedCouponSwapDerivative(final SwapFixedCoupon<?> swap, final DayCount dayCount, final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, multicurves);
    final double pvCoeff = 1. / pvbp;
    final double crossCoeff = -1.0 / pvbp / pvbp;
    final double pvbpCoeff = 2.0 * pvSecond / pvbp / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy)
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity pvbpDr2 = METHOD_SWAP.presentValueBasisPointSecondOrderCurveSensitivity(swap, dayCount, multicurves);

    final int len = swap.getSecondLeg().getNumberOfPayments();
    CouponIbor couponInitial = (CouponIbor) swap.getSecondLeg().getPayments()[0];
    MulticurveSensitivity pvSecondDr2 = CouponIborDiscountingMethod.getInstance().presentValueSecondOrderCurveSensitivity(couponInitial, multicurves).getSensitivity(ccy);
    for (int i = 1; i < len; ++i) {
      CouponIbor coupon = (CouponIbor) swap.getSecondLeg().getPayments()[i];
      pvSecondDr2.plus(CouponIborDiscountingMethod.getInstance().presentValueSecondOrderCurveSensitivity(coupon, multicurves).getSensitivity(ccy));
    }

    final MulticurveSensitivity result = pvSecondDr2.multipliedBy(pvCoeff).plus(pvbpDr2.multipliedBy(pvbpCoeff)).plus(pvSecondDr.productOf(pvbpDr.multipliedBy(crossCoeff)))
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    return result;
  }
}
