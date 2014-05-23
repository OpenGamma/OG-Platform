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
public class RecoveryRateModelConstantTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final static double recoveryRate = 0.563;

  RecoveryRateModelConstant recoveryRateModel = new RecoveryRateModelConstant(recoveryRate);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(enabled = false)
  public void ConstantRecoveryRateModelTest() {

    System.out.println("Running constant recovery rate model test ...");

    System.out.println(recoveryRateModel.getRecoveryRate());

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
