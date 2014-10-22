/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflationissuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.inflation.InflationIssuerProviderAdapter;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;

/**
 * 
 */
public final class PresentValueCurveSensitivityInflationIssuerCalculator 
  extends InstrumentDerivativeVisitorDelegate<ParameterInflationIssuerProviderInterface, MultipleCurrencyInflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityInflationIssuerCalculator INSTANCE = new PresentValueCurveSensitivityInflationIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityInflationIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivityInflationIssuerCalculator() {
    super(new InflationIssuerProviderAdapter<>(PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance()));
  }

  /** Calculators */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_CAPIND_BOND_TR = 
      BondCapitalIndexedTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultipleCurrencyInflationSensitivity visitDepositCounterpart(final DepositCounterpart deposit, 
      final ParameterInflationIssuerProviderInterface issuercurves) {
    return MultipleCurrencyInflationSensitivity.of(
        METHOD_DEPO_CTPY.presentValueCurveSensitivity(deposit, issuercurves.getIssuerProvider()));
  }
 
  //     -----     Bond/Bill     -----

  @Override
  public MultipleCurrencyInflationSensitivity visitBillTransaction(final BillTransaction bill, 
      final ParameterInflationIssuerProviderInterface issuercurves) {
    return MultipleCurrencyInflationSensitivity.of(
        METHOD_BILL_TR.presentValueCurveSensitivity(bill, issuercurves.getIssuerProvider()));
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitBondFixedTransaction(final BondFixedTransaction bond, 
      final ParameterInflationIssuerProviderInterface issuercurves) {
    return MultipleCurrencyInflationSensitivity.of(
        METHOD_BOND_TR.presentValueCurveSensitivity(bond, issuercurves.getIssuerProvider()));
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface curves) {
    return METHOD_CAPIND_BOND_TR.presentValueCurveSensitivity(bond, curves);
  }

}
