/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate modified duration from the curves.
 */
public final class ModifiedDurationFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final ModifiedDurationFromCurvesCalculator s_instance = new ModifiedDurationFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ModifiedDurationFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ModifiedDurationFromCurvesCalculator() {
  }

  /** The method used for bonds */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.modifiedDurationFromCurves(bond, issuer);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.modifiedDurationFromCurves(bond.getBondTransaction(), issuer);
  }
}
