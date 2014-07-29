/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import static com.opengamma.financial.convention.yield.SimpleYieldConvention.INDEX_LINKED_FLOAT;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate accrued interest from clean price.
 */
public final class AccruedInterestFromCleanPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final AccruedInterestFromCleanPriceCalculator s_instance = new AccruedInterestFromCleanPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static AccruedInterestFromCleanPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private AccruedInterestFromCleanPriceCalculator() {
  }

  /**
   * The method used for discounting the bond cashflows.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();
  /**
   * The method used for discounting the inflation bond cashflows.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double cleanPrice) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(cleanPrice, "cleanPrice");
    return METHOD_BOND_SECURITY.accruedInterestFromCleanPrice(bond, cleanPrice) * 100;
  }
  
  @Override
  public Double visitBondFixedTransaction(BondFixedTransaction bond, Double cleanPrice) {
    return visitBondFixedSecurity(bond.getBondTransaction(), cleanPrice);
  }
  
  @Override
  public Double visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, Double cleanRealPrice) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(cleanRealPrice, "yield");
    double accruedInterest = METHOD_INFLATION_BOND_SECURITY.accruedInterestFromCleanRealPrice(bond, cleanRealPrice) * 100;
    if (bond.getYieldConvention().equals(INDEX_LINKED_FLOAT)) {
      return accruedInterest * bond.getIndexRatio();
    } else {
      return accruedInterest;
    }
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final Double cleanPrice) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(cleanPrice, "yield");
    if (!(bond.getBondStandard() instanceof BondCapitalIndexedSecurity<?>)) {
      throw new IllegalArgumentException("Bond should be a BondCapitalIndexedSecurity");
    }
    final BondCapitalIndexedSecurity<?> bondSecurity = (BondCapitalIndexedSecurity<?>) bond.getBondStandard();
    return visitBondCapitalIndexedSecurity(bondSecurity, cleanPrice);
  }

}
