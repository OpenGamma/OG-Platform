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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.method.PricingProviderMethod;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of cash deposit by discounting.
 */
public final class CashDiscountingProviderMethod implements PricingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final CashDiscountingProviderMethod INSTANCE = new CashDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CashDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CashDiscountingProviderMethod() {
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
    double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return MultipleCurrencyAmount.of(deposit.getCurrency(), pv);
  }

  @Override
  public MultipleCurrencyAmount presentValue(InstrumentDerivative instrument, MulticurveProviderInterface curves) {
    Validate.isTrue(instrument instanceof Cash, "Cash");
    return presentValue((Cash) instrument, curves);
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The present value.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueCurveSensitivity(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(new DoublesPair(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    MultipleCurrencyCurveSensitivityMarket result = new MultipleCurrencyCurveSensitivityMarket();
    result = result.plus(deposit.getCurrency(), CurveSensitivityMarket.ofYieldDiscounting(mapDsc));
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
    double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    return (deposit.getInitialAmount() * dfStart - (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor() * dfEnd);
  }

  /**
   * Computes the par spread curve sensitivity.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param curves The curves.
   * @return The spread curve sensitivity.
   */
  public CurveSensitivityMarket parSpreadCurveSensitivity(final Cash deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(curves, "Multicurves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    // Backward sweep
    double parSpreadBar = 1.0;
    double dfEndBar = -(deposit.getInitialAmount() * dfStart / (dfEnd * dfEnd)) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    double dfStartBar = (deposit.getInitialAmount() / dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(new DoublesPair(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    return CurveSensitivityMarket.ofYieldDiscounting(mapDsc);
  }

}
