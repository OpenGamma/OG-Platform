/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of cash deposit by discounting.
 * @deprecated Use {@link CashDiscountingMethod}
 */
@Deprecated
public final class CashDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CashDiscountingMethod INSTANCE = new CashDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CashDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CashDiscountingMethod() {
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final Cash deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    final double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return CurrencyAmount.of(deposit.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof Cash, "Cash");
    return presentValue((Cash) instrument, curves);
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final Cash deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    final double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(deposit.getYieldCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

  /**
   * Computes the deposit fair rate given the start and end time and the accrual factor.
   * When deposit has already start the number may not be meaningful as the remaining period is not in line with the accrual factor.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The rate.
   */
  public double parRate(final Cash deposit, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve dsc = curves.getCurve(deposit.getYieldCurveName());
    final double startTime = deposit.getStartTime();
    final double endTime = deposit.getEndTime();
    final double af = deposit.getAccrualFactor();
    return (dsc.getDiscountFactor(startTime) / dsc.getDiscountFactor(endTime) - 1) / af;
  }

  /**
   * Computes the spread to be added to the deposit rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread.
   */
  public double parSpread(final Cash deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    return (deposit.getInitialAmount() * dfStart - (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor() * dfEnd);
  }

  /**
   * Computes the par spread curve sensitivity.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread curve sensitivity.
   */
  public InterestRateCurveSensitivity parSpreadCurveSensitivity(final Cash deposit, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    final double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double dfEndBar = -(deposit.getInitialAmount() * dfStart / (dfEnd * dfEnd)) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final double dfStartBar = (deposit.getInitialAmount() / dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(deposit.getYieldCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

}
