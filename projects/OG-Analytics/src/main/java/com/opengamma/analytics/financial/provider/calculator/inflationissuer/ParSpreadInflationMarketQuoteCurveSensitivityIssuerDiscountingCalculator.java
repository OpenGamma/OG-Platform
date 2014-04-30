/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflationissuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.inflation.InflationIssuerProviderAdapter;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;

/**
 * Calculates the sensitivity of the par spread (to the market quote) of issuer-specific
 * instruments to the curves used in pricing by discounting. This calculator requires the
 * transaction version of instruments like bonds and bills, as the purchase price
 * information is necessary to calculate a meaningful par spread.
 */
public final class ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<InflationIssuerProviderInterface, InflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator INSTANCE = new ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator() {
    super(new InflationIssuerProviderAdapter<>(ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance()));
  }

  /** Calculator for deposits */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  /** Calculator for bill transactions */
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  /** Calculator for bond transactions */
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public InflationSensitivity visitDepositCounterpart(final DepositCounterpart deposit, final InflationIssuerProviderInterface issuercurves) {
    return InflationSensitivity.of(METHOD_DEPO_CTPY.parSpreadCurveSensitivity(deposit, issuercurves.getIssuerProvider()));
  }

  //     -----     Bond/Bill     -----

  @Override
  public InflationSensitivity visitBillTransaction(final BillTransaction bill, final InflationIssuerProviderInterface issuercurves) {
    return InflationSensitivity.of(METHOD_BILL_TR.parSpreadCurveSensitivity(bill, issuercurves.getIssuerProvider()));
  }

  @Override
  public InflationSensitivity visitBondFixedTransaction(final BondFixedTransaction bond, final InflationIssuerProviderInterface issuercurves) {
    return InflationSensitivity.of(METHOD_BOND_TR.parSpreadCurveSensitivity(bond, issuercurves.getIssuerProvider()));
  }

  // TODO : add inflation bonds

}
