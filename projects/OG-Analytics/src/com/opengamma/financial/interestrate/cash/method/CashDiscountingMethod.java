/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of cash deposit by discounting.
 */
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
    Validate.notNull(deposit);
    Validate.notNull(curves);
    double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return CurrencyAmount.of(deposit.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof Cash, "Cash");
    return presentValue((Cash) instrument, curves);
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final Cash deposit, final YieldCurveBundle curves) {
    Validate.notNull(deposit);
    Validate.notNull(curves);
    double dfStart = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getStartTime());
    double dfEnd = curves.getCurve(deposit.getYieldCurveName()).getDiscountFactor(deposit.getEndTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(new DoublesPair(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(deposit.getYieldCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

  /**
   * Computes the deposit fair rate given the start and end time and the accrual factor. 
   * When deposit has already start the number may not be meaning full as the remaining period is not in line with the accrual factor.
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

}
