/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of deposit by discounting.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class DepositZeroDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final DepositZeroDiscountingMethod INSTANCE = new DepositZeroDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static DepositZeroDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private DepositZeroDiscountingMethod() {
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final DepositZero deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    final double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return CurrencyAmount.of(deposit.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof DepositZero, "Cash");
    return presentValue((DepositZero) instrument, curves);
  }

  /**
   * Compute the present value curve sensitivity by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final DepositZero deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    final double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(deposit.getDiscountingCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

  /**
   * Computes the deposit fair rate given the start and end time and the accrual factor.
   * When deposit has already start the number may not be meaning full as the remaining period is not in line with the accrual factor.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The rate.
   */
  public double parRate(final DepositZero deposit, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve dsc = curves.getCurve(deposit.getDiscountingCurveName());
    final double startTime = deposit.getStartTime();
    final double endTime = deposit.getEndTime();
    final double rcc = Math.log(dsc.getDiscountFactor(startTime) / dsc.getDiscountFactor(endTime)) / deposit.getPaymentAccrualFactor();
    final InterestRate rate = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc));
    return rate.getRate();
  }

  /**
   * Computes the deposit fair rate curve sensitivity.
   * When deposit has already start the number may not be meaning full as the remaining period is not in line with the accrual factor.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The rate sensitivity.
   */
  public InterestRateCurveSensitivity parRateCurveSensitivity(final DepositZero deposit, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve dsc = curves.getCurve(deposit.getDiscountingCurveName());
    final double dfStartTime = dsc.getDiscountFactor(deposit.getStartTime());
    final double dfEndTime = dsc.getDiscountFactor(deposit.getEndTime());
    final double rcc = Math.log(dfStartTime / dfEndTime) / deposit.getPaymentAccrualFactor();
    final double rateBar = 1.0;
    final double rccBar = deposit.getRate().fromContinuousDerivative(new ContinuousInterestRate(rcc)) * rateBar;
    final double dfEndTimeBar = -1.0 / dfEndTime / deposit.getPaymentAccrualFactor() * rccBar;
    final double dfStartTimeBar = 1.0 / dfStartTime / deposit.getPaymentAccrualFactor() * rccBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStartTime * dfStartTimeBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEndTime * dfEndTimeBar));
    resultMapDsc.put(deposit.getDiscountingCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

  /**
   * Computes the spread to be added to the deposit rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as the remaining period is not in line with the accrual factor.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread.
   */
  public double parSpread(final DepositZero deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    final double ccrs = Math.log(deposit.getInitialAmount() * dfStart / (deposit.getNotional() * dfEnd)) / deposit.getPaymentAccrualFactor();
    final InterestRate rs = deposit.getRate().fromContinuous(new ContinuousInterestRate(ccrs));
    return rs.getRate() - deposit.getRate().getRate();
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread curve sensitivity.
   */
  public InterestRateCurveSensitivity parSpreadCurveSensitivity(final DepositZero deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    final double ccrs = Math.log(deposit.getInitialAmount() * dfStart / (deposit.getNotional() * dfEnd)) / deposit.getPaymentAccrualFactor();
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double rsBar = parSpreadBar;
    final double ccrsBar = deposit.getRate().fromContinuousDerivative(new ContinuousInterestRate(ccrs)) * rsBar;
    final double dfEndBar = -1 / (dfEnd * deposit.getPaymentAccrualFactor()) * ccrsBar;
    final double dfStartBar = 1 / (dfEnd * deposit.getPaymentAccrualFactor()) * ccrsBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(deposit.getDiscountingCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

}
