/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the sensitivity of the present value of issuer-specific instruments to curves
 * used in pricing by discounting.
 */
public final class PresentValueCurveSensitivityIssuerCalculator extends InstrumentDerivativeVisitorDelegate<ParameterIssuerProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityIssuerCalculator INSTANCE = new PresentValueCurveSensitivityIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivityIssuerCalculator() {
    super(new IssuerProviderAdapter<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance()));
  }

  /** Method for counterparty deposits */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  /** Method for bill securities */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SEC = BillSecurityDiscountingMethod.getInstance();
  /** Method for bill transactions */
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  /** Method for bond securities */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();
  /** Method for bond transactions */
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  /** Method for bond future transactions */
  private static final BondFuturesTransactionDiscountingMethod METHOD_BNDFUT_TRA = BondFuturesTransactionDiscountingMethod.getInstance();
  /** Method for bond Total Return Swap */
  private static final BondTotalReturnSwapDiscountingMethod METHOD_TRS_BND = BondTotalReturnSwapDiscountingMethod.getInstance();
  /** Method for bill Total Return Swap */
  private static final BillTotalReturnSwapDiscountingMethod METHOD_TRS_BILL = BillTotalReturnSwapDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitDepositCounterpart(final DepositCounterpart deposit, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.presentValueCurveSensitivity(deposit, issuercurves.getIssuerProvider());
  }

  //     -----     Bond/Bill     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBillSecurity(final BillSecurity bill, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BILL_SEC.presentValueCurveSensitivity(bill, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBillTransaction(final BillTransaction bill, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.presentValueCurveSensitivity(bill, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFixedSecurity(final BondFixedSecurity bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_SEC.presentValueCurveSensitivity(bond, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFixedTransaction(final BondFixedTransaction bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.presentValueCurveSensitivity(bond, issuercurves.getIssuerProvider());
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesTransaction(final BondFuturesTransaction futures, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BNDFUT_TRA.presentValueCurveSensitivity(futures, issuercurves.getIssuerProvider());
  }

  //     -----     Other     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondTotalReturnSwap(final BondTotalReturnSwap trs, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_TRS_BND.presentValueCurveSensitivity(trs, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBillTotalReturnSwap(final BillTotalReturnSwap trs, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_TRS_BILL.presentValueCurveSensitivity(trs, issuercurves.getIssuerProvider());
  }

}
