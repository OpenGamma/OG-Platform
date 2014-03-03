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

import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of Ibor fixing.
 */
public final class DepositIborDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final DepositIborDiscountingMethod INSTANCE = new DepositIborDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static DepositIborDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private DepositIborDiscountingMethod() {
  }

  /**
   * Compute the present value by discounting of a "Ibor deposit", i.e. a fictitious deposit representing the Ibor fixing.
   * @param deposit The deposit.
   * @param multicurves The multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final DepositIbor deposit, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double pv = deposit.getAccrualFactor() * (deposit.getRate() - multicurves.getSimplyCompoundForwardRate(deposit.getIndex(), deposit.getStartTime(),
        deposit.getEndTime(), deposit.getAccrualFactor())) * dfEnd;
    return MultipleCurrencyAmount.of(deposit.getCurrency(), pv);
  }

  /**
   * Compute the present value curve sensitivity by discounting.
   * @param deposit The deposit.
   * @param multicurves The multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final DepositIbor deposit, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurves, "Multicurves");
    final double dfEnd = multicurves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double forward = multicurves.getSimplyCompoundForwardRate(deposit.getIndex(), deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor());
    // Backward sweep
    final double forwardBar = deposit.getAccrualFactor() * dfEnd;
    final double dfEndBar = deposit.getAccrualFactor() * (deposit.getRate() - forward);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(deposit.getIndex()), listForward);
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(multicurves.getName(deposit.getCurrency()), listDiscounting);
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    result = result.plus(deposit.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

  /**
   * Computes the spread to be added to the Ibor rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param multicurve The curves.
   * @return The spread.
   */
  public double parSpread(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return multicurve.getSimplyCompoundForwardRate(deposit.getIndex(), deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor()) - deposit.getRate();
  }

  /**
   * Computes the par spread curve sensitivity.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param multicurve The curves.
   * @return The spread curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurve, "Multicurves");
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor(), 1.0));
    mapFwd.put(multicurve.getName(deposit.getIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
