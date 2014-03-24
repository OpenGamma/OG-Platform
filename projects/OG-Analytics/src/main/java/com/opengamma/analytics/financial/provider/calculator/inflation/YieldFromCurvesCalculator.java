/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class YieldFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<InflationProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final YieldFromCurvesCalculator s_instance = new YieldFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static YieldFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /** The method used for bonds */
  // TODO : use the method with issuer when inflaiton curves with issuer are integrated.
  private static final BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer METHOD_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer.getInstance();

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final InflationProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.yieldRealFromCurves(bond.getBondTransaction(), issuer);
  }

  @Override
  public Double visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuer) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(issuer, "Issuer provider");
    return METHOD_BOND_SECURITY.yieldRealFromCurves(bond, issuer);
  }

}
