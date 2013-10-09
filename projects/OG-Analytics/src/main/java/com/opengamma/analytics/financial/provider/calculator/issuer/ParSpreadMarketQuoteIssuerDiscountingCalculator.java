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
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteIssuerDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<IssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteIssuerDiscountingCalculator() {
    super(new IssuerProviderAdapter<>(ParSpreadMarketQuoteDiscountingCalculator.getInstance()));
  }

  /**
   * Pricing methods.
   */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public Double visitDepositCounterpart(final DepositCounterpart deposit, final IssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.parSpread(deposit, issuercurves);
  }

  //     -----     Bond/Bill     -----

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final IssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.parSpread(bill, issuercurves);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.parSpread(bond, issuercurves);
  }

}
