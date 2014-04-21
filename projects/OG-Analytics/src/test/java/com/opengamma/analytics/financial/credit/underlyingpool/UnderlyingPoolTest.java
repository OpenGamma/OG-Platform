/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests to verify the correct construction of an underlying pool object 
 */
@Test(groups = TestGroup.UNIT)
public class UnderlyingPoolTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add the tests to check if an element in a vector is 'null'

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define the composition of the underlying pool

  private static final String poolName = "Test_1";

  private static final int numberOfObligors = 3;
  private static final int numberOfTenors = 4;

  private static final LegalEntity[] obligors = new LegalEntity[numberOfObligors];

  private static final double[] notionals = new double[numberOfObligors];
  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Currency[] currency = new Currency[numberOfObligors];
  private static final DebtSeniority[] debtSeniority = new DebtSeniority[numberOfObligors];
  private static final RestructuringClause[] restructuringClause = new RestructuringClause[numberOfObligors];

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoolNameField() {

    new UnderlyingPool(null, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorsField() {

    new UnderlyingPool(poolName, null, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyField() {

    new UnderlyingPool(poolName, obligors, null, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDebtSeniorityField() {

    new UnderlyingPool(poolName, obligors, currency, null, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRestructuringClauseField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, null, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditSpreadTenorsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditSpreadTermStructuresField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNotionalsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, null, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCouponsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, null, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRecoveryRatesField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, null, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorWeightsField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, null);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYieldCurveField() {

    new UnderlyingPool(poolName, obligors, currency, debtSeniority, restructuringClause, notionals, coupons, recoveryRates, obligorWeights);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
