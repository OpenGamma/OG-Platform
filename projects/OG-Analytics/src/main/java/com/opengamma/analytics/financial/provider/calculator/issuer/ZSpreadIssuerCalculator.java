/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.tuple.Pair;

/**
* Calculates the z-spread of instruments using issuer-specific curves.
*/
public final class ZSpreadIssuerCalculator extends
    InstrumentDerivativeVisitorAdapter<Pair<IssuerProviderInterface, Double>, Double> {

  private static final ZSpreadIssuerCalculator INSTANCE = new ZSpreadIssuerCalculator();

  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();

  private static final int SCALING_FACTOR = 100;

  private ZSpreadIssuerCalculator() {}

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ZSpreadIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /** necessary to scale twice, to scale up for the input price and the calculator output */
  private Double getBondZSpreadPrice(BondSecurity bond, Pair<IssuerProviderInterface, Double> data) {
    return SCALING_FACTOR * SCALING_FACTOR * METHOD_BOND_SEC.zSpreadFromCurvesAndClean(bond,
                                                                                       data.getFirst(),
                                                                                       data.getSecond());
  }

  //TODO add BondIborSecurity, BondInterestIndexedSecurity when clean price is supported

  @Override
  public Double visitBondFixedSecurity(BondFixedSecurity bond, Pair<IssuerProviderInterface, Double> data) {
    return getBondZSpreadPrice(bond, data);
  }

  @Override
  public Double visitBondFixedTransaction(BondFixedTransaction bond, Pair<IssuerProviderInterface, Double> data) {
    return getBondZSpreadPrice(bond.getBondStandard(), data);
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond,
                                                   Pair<IssuerProviderInterface, Double> data) {
    return getBondZSpreadPrice(bond.getBondStandard(), data);
  }

}
