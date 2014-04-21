/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NewtonDefaultUpdateFunctionTest {
  private static final NewtonDefaultUpdateFunction F = new NewtonDefaultUpdateFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    F.getUpdatedMatrix(null, null, null, null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    F.getUpdatedMatrix(new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        return null;
      }
    }, null, null, null, null);
  }

}
