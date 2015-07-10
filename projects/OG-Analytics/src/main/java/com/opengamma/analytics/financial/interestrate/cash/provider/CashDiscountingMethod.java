/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of cash deposit by discounting.
 */
public final class CashDiscountingMethod {

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
  public MultipleCurrencyAmount presentValue(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return MultipleCurrencyAmount.of(deposit.getCurrency(), pv);
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    final double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    result = result.plus(deposit.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    return result;
  }

  /**
   * Computes the deposit fair rate given the start and end time and the accrual factor.
   * When deposit has already start the number may not be meaning full as the remaining period is not in line with the accrual factor.
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The rate.
   */
  public double parRate(final Cash deposit, final MulticurveProviderInterface curves) {
    final double af = deposit.getAccrualFactor();
    return (curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime()) / curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime()) - 1) / af;
  }

  /**
   * Computes the spread to be added to the deposit rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread.
   */
  public double parSpread(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    return (deposit.getInitialAmount() * dfStart - (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor() * dfEnd);
  }

  /**
   * Computes the par spread curve sensitivity.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double dfEndBar = -(deposit.getInitialAmount() * dfStart / (dfEnd * dfEnd)) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final double dfStartBar = (deposit.getInitialAmount() / dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    return MulticurveSensitivity.ofYieldDiscounting(mapDsc);
  }

}
