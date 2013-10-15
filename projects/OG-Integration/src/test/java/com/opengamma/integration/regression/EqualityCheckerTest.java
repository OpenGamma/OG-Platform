/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class EqualityCheckerTest {

  private static final double DELTA = 0.0001;

  @Test
  public void yieldCurve() {
    NodalDoublesCurve nodalDoublesCurve1 = new NodalDoublesCurve(new double[]{1, 2, 3}, new double[]{2, 3, 4}, true);
    assertTrue(EqualityChecker.equals(new YieldCurve("name", nodalDoublesCurve1),
                                      new YieldCurve("name", nodalDoublesCurve1),
                                      DELTA));
    // name is ignored because it's auto-generated and differs between runs
    assertTrue(EqualityChecker.equals(new YieldCurve("name1", nodalDoublesCurve1),
                                      new YieldCurve("name2", nodalDoublesCurve1),
                                      DELTA));
    NodalDoublesCurve nodalDoublesCurve2 = new NodalDoublesCurve(new double[]{1.1, 2, 3}, new double[]{2, 3, 4}, true);
    assertFalse(EqualityChecker.equals(new YieldCurve("name", nodalDoublesCurve1),
                                       new YieldCurve("name", nodalDoublesCurve2),
                                       DELTA));
  }
}
