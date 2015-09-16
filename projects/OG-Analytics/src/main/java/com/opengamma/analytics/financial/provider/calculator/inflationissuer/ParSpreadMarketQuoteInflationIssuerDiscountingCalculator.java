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
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;

/**
 * Calculates the par spread (to the market quote) of issuer-specific instruments by discounting.
 * This calculator requires the transaction version of instruments like bonds and bills, as the
 * purchase price information is necessary to calculate a meaningful par spread.
 */
public final class ParSpreadMarketQuoteInflationIssuerDiscountingCalculator 
  extends InstrumentDerivativeVisitorDelegate<ParameterInflationIssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteInflationIssuerDiscountingCalculator INSTANCE = 
      new ParSpreadMarketQuoteInflationIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteInflationIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ParSpreadMarketQuoteInflationIssuerDiscountingCalculator() {
    super(new InflationIssuerProviderAdapter<>(ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance()));
  }

  /** Calculators */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = BillTransactionDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_CAPINDBOND_TR = 
      BondCapitalIndexedTransactionDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public Double visitDepositCounterpart(final DepositCounterpart deposit, final ParameterInflationIssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.parSpread(deposit, issuercurves.getIssuerProvider());
  }

  //     -----     Bond/Bill     -----

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final ParameterInflationIssuerProviderInterface issuercurves) {
    return METHOD_BILL_TR.parSpread(bill, issuercurves.getIssuerProvider());
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final ParameterInflationIssuerProviderInterface issuercurves) {
    return METHOD_BOND_TR.parSpread(bond, issuercurves.getIssuerProvider());
  }

  //     -----     Bond Inflation     -----
  
  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface curves) {
    return METHOD_CAPINDBOND_TR.parSpread(bond, curves);
  }
  
}
