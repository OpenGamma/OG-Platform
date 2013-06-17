/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 *
 */
public class YieldCurveSingleShiftTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void fudgeRoundTrip() {
    YieldCurveSingleShift shift = new YieldCurveSingleShift(0.123, 0.321);
    assertEncodeDecodeCycle(YieldCurveSingleShift.class, shift);
  }
}
