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
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an instruments using issuer-specific curves.
 */
public final class PresentValueIssuerCalculator extends InstrumentDerivativeVisitorDelegate<IssuerProviderInterface, MultipleCurrencyAmount> {

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

  /**
   * Pricing methods.
   */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  private static final BillSecurityDiscountingMethod METHOD_BILL_SEC = BillSecurityDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  private static final BondFutureDiscountingMethod METHOD_BNDFUT_DSC = BondFutureDiscountingMethod.getInstance();
  private static final BondFuturesTransactionDiscountingMethod METHOD_BNDFUT_TRA = BondFuturesTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultipleCurrencyAmount visitDepositCounterpart(final DepositCounterpart deposit, final IssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.presentValue(deposit, issuercurves);
  }

  //     -----     Bond/Bill     -----

  @Override
  public MultipleCurrencyAmount visitBillSecurity(final BillSecurity bill, final IssuerProviderInterface issuercurves) {
    return METHOD_BILL_SEC.presentValue(bill, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBillTransaction(final BillTransaction bill, final IssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.presentValue(bill, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_SEC.presentValue(bond, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBondIborSecurity(final BondIborSecurity bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_SEC.presentValue(bond, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.presentValue(bond, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBondIborTransaction(final BondIborTransaction bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.presentValue(bond, issuercurves);
  }

  //     -----     Futures     -----

  @Override
  // TODO: Remove
  public MultipleCurrencyAmount visitBondFuture(final BondFuture futures, final IssuerProviderInterface issuercurves) {
    return METHOD_BNDFUT_DSC.presentValue(futures, issuercurves);
  }

  @Override
  public MultipleCurrencyAmount visitBondFuturesTransaction(final BondFuturesTransaction futures, final IssuerProviderInterface issuercurves) {
    return METHOD_BNDFUT_TRA.presentValue(futures, issuercurves);
  }
}
