/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RecoveryRateModelStochasticTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final static double a = 0.75;
  private final static double b = 0.75;

  private final static double x = 0.7;

  RecoveryRateModelStochastic recoveryRateModel = new RecoveryRateModelStochastic(a, b, x);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(enabled = false)
  public void ConstantRecoveryRateModelTest() {

    System.out.println("Running constant recovery rate model test ...");

    RecoveryRateModelStochastic recRateModel = recoveryRateModel;

    for (double x = 0.01; x <= 1.0; x += 0.01) {

      recRateModel = recRateModel.sampleRecoveryRate(x);

      System.out.println("a = " + a + "\t" + "b = " + b + "\t" + "x = " + "\t" + x + "\t" + "delta = " + "\t" + recRateModel.getRecoveryRate());
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
