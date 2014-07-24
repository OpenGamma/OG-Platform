/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate clean price from the curves.
 */
public final class CleanPriceFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final CleanPriceFromCurvesCalculator s_instance = new CleanPriceFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static CleanPriceFromCurvesCalculator getInstance() {
    return s_instance;
  }

  // TODO : use the method with issuer when inflation curves with issuer are integrated.
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY =
      BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  /**
   * Scaling factor for clean price
   */
  private static final int SCALING_FACTOR = 100;

  /**
   * Private constructor.
   */
  private CleanPriceFromCurvesCalculator() {
  }

  //TODO add BondIborSecurity, BondInterestIndexedSecurity

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return SCALING_FACTOR * METHOD_BOND_SECURITY.cleanPriceFromCurves(bond, issuer);
  }
  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return SCALING_FACTOR * METHOD_BOND_SECURITY.cleanPriceFromCurves(bond.getBondStandard(), issuer);
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    final InflationIssuerProviderInterface inflationIssuer = (InflationIssuerProviderInterface) issuer;
    return SCALING_FACTOR * METHOD_INFLATION_BOND_SECURITY.cleanRealPriceFromCurves(bond.getBondStandard(), inflationIssuer);
  }
}
