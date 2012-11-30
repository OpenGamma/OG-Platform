/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.util.money.Currency;

/**
 * Tests to verify the correct construction of an underlying pool object 
 */
public class UnderlyingPoolTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add the tests to check if an element in a vector is 'null'

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define the composition of the underlying pool

  private static final String poolName = "Test_1";

  private static final int numberOfObligors = 3;
  private static final int numberOfTenors = 4;

  private static final Obligor[] obligors = new Obligor[numberOfObligors];

  private static final double[] notionals = new double[numberOfObligors];
  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Currency[] currency = new Currency[numberOfObligors];
  private static final DebtSeniority[] debtSeniority = new DebtSeniority[numberOfObligors];
  private static final RestructuringClause[] restructuringClause = new RestructuringClause[numberOfObligors];

  private static final CreditSpreadTenors[] creditSpreadTenors = new CreditSpreadTenors[numberOfTenors];
  private static final double[][] spreadTermStructures = new double[numberOfObligors][numberOfTenors];

  private static final ISDACurve[] yieldCurve = new ISDACurve[numberOfObligors];

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoolNameField() {

    new UnderlyingPool(null, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorsField() {

    new UnderlyingPool(poolName, null, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyField() {

    new UnderlyingPool(poolName, obligors, null, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDebtSeniorityField() {

    new UnderlyingPool(poolName, obligors, currency, null, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRestructuringClauseField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, null, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditSpreadTenorsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, null, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditSpreadTermStructuresField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, null, notionals, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNotionalsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, null, coupons, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCouponsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, null, recoveryRates, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRecoveryRatesField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, null, obligorWeights, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorWeightsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, null, yieldCurve);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYieldCurveField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, creditSpreadTenors, spreadTermStructures, notionals, coupons, recoveryRates, obligorWeights, null);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
