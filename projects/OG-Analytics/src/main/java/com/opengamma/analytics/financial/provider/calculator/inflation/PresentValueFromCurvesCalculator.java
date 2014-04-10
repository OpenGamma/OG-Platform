/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class PresentValueFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<InflationIssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final PresentValueFromCurvesCalculator s_instance = new PresentValueFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PresentValueFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueFromCurvesCalculator() {
  }

  /** The method used for bonds */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_TRANSACTION = BondCapitalIndexedTransactionDiscountingMethod.getInstance();

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final InflationIssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_TRANSACTION.presentValue(bond, issuer).getAmount(bond.getBondStandard().getCurrency()) * 100;
  }

  @Override
  public Double visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final InflationIssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.presentValue(bond, issuer).getAmount(bond.getCurrency()) * 100;
  }
}
