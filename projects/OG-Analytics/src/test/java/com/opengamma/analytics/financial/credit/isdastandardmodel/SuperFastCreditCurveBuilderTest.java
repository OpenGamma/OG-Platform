/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.SuperFastCreditCurveBuilder;

/**
 * 
 */
public class SuperFastCreditCurveBuilderTest extends CreditCurveCalibrationTest {

  private static final SuperFastCreditCurveBuilder BUILDER_ISDA = new SuperFastCreditCurveBuilder();
  private static final SuperFastCreditCurveBuilder BUILDER_MARKIT = new SuperFastCreditCurveBuilder(MARKIT_FIX);

  @Test
  public void test() {
    testCalibrationAgainstISDA(BUILDER_ISDA, 1e-14);

    //NOTE: we do not match the Markit 'fix' for forward starting swaps 
    // testCalibrationAgainstISDA(BUILDER_MARKIT, 1e-14);
  }

}
