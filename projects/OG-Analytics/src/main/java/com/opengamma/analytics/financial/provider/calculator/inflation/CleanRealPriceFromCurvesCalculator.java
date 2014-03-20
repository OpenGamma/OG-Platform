/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate clean price from the curves.
 */
public final class CleanRealPriceFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<InflationProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final CleanRealPriceFromCurvesCalculator s_instance = new CleanRealPriceFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static CleanRealPriceFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private CleanRealPriceFromCurvesCalculator() {
  }

  /** The method used for bonds */
  // TODO : use the method with issuer when inflaiton curves with issuer are integrated.
  private static final BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer METHOD_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer.getInstance();

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final InflationProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.cleanRealPriceFromCurves(bond.getBondTransaction(), issuer) * 100;
  }
}
