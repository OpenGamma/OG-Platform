/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of instruments using issuer-specific curves.
 */
public final class PresentValueIssuerCalculator extends InstrumentDerivativeVisitorDelegate<ParameterIssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueIssuerCalculator INSTANCE = new PresentValueIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueIssuerCalculator() {
    super(new IssuerProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
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
  private static final BondTotalReturnSwapDiscountingMethod METHOD_TRS = BondTotalReturnSwapDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultipleCurrencyAmount visitDepositCounterpart(final DepositCounterpart deposit, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.presentValue(deposit, issuercurves.getIssuerProvider());
  }

  //     -----     Bond/Bill     -----

  @Override
  public MultipleCurrencyAmount visitBillSecurity(final BillSecurity bill, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BILL_SEC.presentValue(bill, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyAmount visitBillTransaction(final BillTransaction bill, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.presentValue(bill, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyAmount visitBondFixedSecurity(final BondFixedSecurity bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_SEC.presentValue(bond, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyAmount visitBondIborSecurity(final BondIborSecurity bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_SEC.presentValue(bond, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyAmount visitBondFixedTransaction(final BondFixedTransaction bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.presentValue(bond, issuercurves.getIssuerProvider());
  }

  @Override
  public MultipleCurrencyAmount visitBondIborTransaction(final BondIborTransaction bond, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.presentValue(bond, issuercurves.getIssuerProvider());
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyAmount visitBondFuturesTransaction(final BondFuturesTransaction futures, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_BNDFUT_TRA.presentValue(futures, issuercurves.getIssuerProvider());
  }

  //     -----     Other     -----

  @Override
  public MultipleCurrencyAmount visitBondTotalReturnSwap(final BondTotalReturnSwap trs, final ParameterIssuerProviderInterface issuercurves) {
    return METHOD_TRS.presentValue(trs, issuercurves.getIssuerProvider());
  }

}
