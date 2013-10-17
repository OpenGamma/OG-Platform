/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.testng.annotations.Test;

/**
 * 
 */
public class FastCreditCurveBuilderTest extends CreditCurveCalibrationTest {

  private static final FastCreditCurveBuilder BUILDER_ISDA = new FastCreditCurveBuilder();
  private static final FastCreditCurveBuilder BUILDER_MARKIT = new FastCreditCurveBuilder(MARKIT_FIX);

  @Test
  public void test() {
    testCalibrationAgainstISDA(BUILDER_ISDA, 1e-14);
    testCalibrationAgainstISDA(BUILDER_MARKIT, 1e-14);
  }

}
