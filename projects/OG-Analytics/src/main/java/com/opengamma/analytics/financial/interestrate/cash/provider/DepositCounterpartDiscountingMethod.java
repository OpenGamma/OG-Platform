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

import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of cash deposit by discounting.
 */
public final class DepositCounterpartDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final DepositCounterpartDiscountingMethod INSTANCE = new DepositCounterpartDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static DepositCounterpartDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private DepositCounterpartDiscountingMethod() {
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param multicurves The multi-curves and issuer curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final DepositCounterpart deposit, final IssuerProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    final double dfStart = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getStartTime());
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getEndTime());
    final double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return MultipleCurrencyAmount.of(deposit.getCurrency(), pv);
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   * @param deposit The deposit.
   * @param multicurves The multi-curves and issuer curves provider.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final DepositCounterpart deposit, final IssuerProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    final double dfStart = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getStartTime());
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    final double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(multicurves.getName(deposit.getCounterparty()), listDiscounting);
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    result = result.plus(deposit.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    return result;
  }

  /**
   * Computes the spread to be added to the deposit rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param multicurves The multi-curves and issuer curves provider.
   * @return The spread.
   */
  public double parSpread(final DepositCounterpart deposit, final IssuerProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getStartTime());
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getEndTime());
    return (deposit.getInitialAmount() * dfStart - (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor() * dfEnd);
  }

  /**
   * Computes the par spread curve sensitivity.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param multicurves The multi-curves and issuer curves provider.
   * @return The spread curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final DepositCounterpart deposit, final IssuerProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    ArgumentChecker.isTrue(deposit.getNotional() != 0.0, "Notional is 0");
    final double dfStart = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getStartTime());
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCounterparty(), deposit.getEndTime());
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double dfEndBar = -(deposit.getInitialAmount() * dfStart / (dfEnd * dfEnd)) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final double dfStartBar = (deposit.getInitialAmount() / dfEnd) / (deposit.getNotional() * deposit.getAccrualFactor()) * parSpreadBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(multicurves.getName(deposit.getCounterparty()), listDiscounting);
    return MulticurveSensitivity.ofYieldDiscounting(mapDsc);
  }

}
