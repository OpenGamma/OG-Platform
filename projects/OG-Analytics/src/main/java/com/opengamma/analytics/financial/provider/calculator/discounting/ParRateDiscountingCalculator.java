/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.financial.convention.daycount.DayCount;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class ParRateDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateDiscountingCalculator INSTANCE = new ParRateDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParRateDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParRateDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPO = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();

  //     -----     Deposit     ------

  @Override
  public Double visitCash(final Cash deposit, final MulticurveProviderInterface multicurves) {
    return METHOD_DEPO.parRate(deposit, multicurves);
  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurves) {
    return METHOD_IBOR.parRate(deposit, multicurves);
  }

  //     -----     Payment/Coupon     ------

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurves) {
    return METHOD_FRA.parRate(fra, multicurves);
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface data) {
    double fwd = data.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(),
        payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
    return fwd;
  }

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final MulticurveProviderInterface data) {
    return visitCouponIborSpread(payment.toCoupon(), data);
  }

  //     -----     Swap     -----

  /**
   * Computes the par rate of a swap with one fixed leg.
   * @param swap The Fixed coupon swap.
   * @param multicurves The multi-curves provider.
   * @return The par swap rate. If the fixed leg has been set up with some fixed payments these are ignored for the purposes of finding the swap rate
   */
  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    final double pvSecond = swap.getSecondLeg().accept(PVC, multicurves).getAmount(swap.getSecondLeg().getCurrency()) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, multicurves);
    return pvSecond / pvbp;
  }

  /**
   * Computes the swap convention-modified par rate for a fixed coupon swap.
   * <P>Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   * @param swap The swap.
   * @param dayCount The day count convention to modify the swap rate.
   * @param multicurves The multi-curves provider.
   * @return The modified rate.
   */
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DayCount dayCount, final MulticurveProviderInterface multicurves) {
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, multicurves);
    return visitFixedCouponSwap(swap, pvbp, multicurves);
  }

  /**
   * Computes the swap convention-modified par rate for a fixed coupon swap with a PVBP externally provided.
   * <P>Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   * @param swap The swap.
   * @param pvbp The present value of a basis point.
   * @param multicurves The multi-curves provider.
   * @return The modified rate.
   */
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final double pvbp, final MulticurveProviderInterface multicurves) {
    final double pvSecond = swap.getSecondLeg().accept(PVC, multicurves).getAmount(swap.getSecondLeg().getCurrency()) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    return pvSecond / pvbp;
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final MulticurveProviderInterface multicurves) {
    return METHOD_IR_FUT.parRate(futures, multicurves);
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_IR_FUT.parRate(futures.getUnderlyingSecurity(), multicurves);
  }

  //     -----     Forex     ------

  /**
   * Computes the forward forex rate.
   * @param forex The forex instrument.
   * @param multicurves The multicurves provider.
   * @return The forward forex rate.
   */
  @Override
  public Double visitForex(final Forex forex, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX.forwardForexRate(forex, multicurves);
  }

}
