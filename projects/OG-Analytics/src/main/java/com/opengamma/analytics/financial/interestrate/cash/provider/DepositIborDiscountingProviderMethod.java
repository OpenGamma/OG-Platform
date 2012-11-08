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
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * The methods associated to the pricing of Ibor fixing.
 */
public final class DepositIborDiscountingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final DepositIborDiscountingProviderMethod INSTANCE = new DepositIborDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static DepositIborDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private DepositIborDiscountingProviderMethod() {
  }

  /**
   * Compute the present value by discounting of a "Ibor deposit", i.e. a fictitious deposit representing the Ibor fixing.
   * @param deposit The deposit.
   * @param multicurve The curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(deposit, "Deposit");
    ArgumentChecker.notNull(multicurve, "Multicurves");
    double dfEnd = multicurve.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    double pv = deposit.getAccrualFactor() * (deposit.getRate() - multicurve.getForwardRate(deposit.getIndex(), deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor())) * dfEnd;
    return MultipleCurrencyAmount.of(deposit.getCurrency(), pv);
  }

  /**
   * Computes the spread to be added to the Ibor rate to have a zero present value.
   * When deposit has already start the number may not be meaning full as only the final payment remains (no initial payment).
   * @param deposit The deposit.
   * @param multicurve The curves.
   * @return The spread.
   */
  public double parSpread(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return multicurve.getForwardRate(deposit.getIndex(), deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor()) - deposit.getRate();
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
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    final List<ForwardSensitivity> listForward = new ArrayList<ForwardSensitivity>();
    listForward.add(new ForwardSensitivity(deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor(), 1.0));
    mapFwd.put(multicurve.getName(deposit.getIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
