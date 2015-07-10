/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveSingleShiftTest extends AbstractFudgeBuilderTestCase {

  public void fudgeRoundTrip() {
    YieldCurveSingleShift shift = new YieldCurveSingleShift(0.123, 0.321);
    assertEncodeDecodeCycle(YieldCurveSingleShift.class, shift);
  }

}
