/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.InflationIssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueDiscountingInflationIssuerCalculator extends AbstractInstrumentDerivativeVisitor<InflationIssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueDiscountingInflationIssuerCalculator INSTANCE = new PresentValueDiscountingInflationIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueDiscountingInflationIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueDiscountingInflationIssuerCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_SEC = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_TR = new BondCapitalIndexedTransactionDiscountingMethod();

  private static final PresentValueDiscountingInflationCalculator PVDIC = PresentValueDiscountingInflationCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final InflationIssuerProviderInterface market) {
    try {
      return derivative.accept(this, market);
    } catch (Exception e) {
      return derivative.accept(PVDIC, market.getInflationProvider());
    }
  }

  @Override
  public MultipleCurrencyAmount visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final InflationIssuerProviderInterface market) {
    return METHOD_BOND_SEC.presentValue(bond, market);
  }

  @Override
  public MultipleCurrencyAmount visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final InflationIssuerProviderInterface market) {
    return METHOD_BOND_TR.presentValue(bond, market);
  }

}
