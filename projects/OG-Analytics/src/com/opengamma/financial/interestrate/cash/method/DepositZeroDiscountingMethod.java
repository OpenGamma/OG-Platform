/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ContinuousInterestRate;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRate;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of deposit by discounting.
 */
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
    Validate.notNull(deposit);
    Validate.notNull(curves);
    double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return CurrencyAmount.of(deposit.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof DepositZero, "Cash");
    return presentValue((DepositZero) instrument, curves);
  }

  /**
   * Compute the present value curve sensitivity by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final DepositZero deposit, final YieldCurveBundle curves) {
    Validate.notNull(deposit);
    Validate.notNull(curves);
    double dfStart = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getStartTime());
    double dfEnd = curves.getCurve(deposit.getDiscountingCurveName()).getDiscountFactor(deposit.getEndTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(new DoublesPair(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
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
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(deposit.getStartTime(), -deposit.getStartTime() * dfStartTime * dfStartTimeBar));
    listDiscounting.add(new DoublesPair(deposit.getEndTime(), -deposit.getEndTime() * dfEndTime * dfEndTimeBar));
    resultMapDsc.put(deposit.getDiscountingCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

}
