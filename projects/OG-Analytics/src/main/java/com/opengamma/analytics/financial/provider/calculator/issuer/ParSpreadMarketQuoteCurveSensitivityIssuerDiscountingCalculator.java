/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Calculates the present value of an ...
 */
public final class ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<IssuerProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator() {
    super(new IssuerProviderAdapter<>(ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance()));
  }

  /**
   * Pricing methods.
   */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MulticurveSensitivity visitDepositCounterpart(final DepositCounterpart deposit, final IssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.parSpreadCurveSensitivity(deposit, issuercurves);
  }

  //     -----     Bond/Bill     -----

  @Override
  public MulticurveSensitivity visitBillTransaction(final BillTransaction bill, final IssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.parSpreadCurveSensitivity(bill, issuercurves);
  }

  @Override
  public MulticurveSensitivity visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.parSpreadCurveSensitivity(bond, issuercurves);
  }

}
