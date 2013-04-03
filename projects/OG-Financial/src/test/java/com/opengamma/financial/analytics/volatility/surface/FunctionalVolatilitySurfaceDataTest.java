/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionalVolatilitySurfaceDataTest {

  private static final String X_LABEL = "X";
  private static final double X_MIN = 0.01;
  private static final double X_MAX = 20;
  private static final int NX = 100;
  private static final String Y_LABEL = "Y";
  private static final double Y_MIN = 0.04;
  private static final double Y_MAX = 14;
  private static final int NY = 200;
  private static final double Z_MIN = 0;
  private static final double Z_MAX = 200;

  @Test
  public void testObject() {
    final FunctionalDoublesSurface f = getSurface();
    final VolatilitySurface vol = new VolatilitySurface(f);
    final FunctionalVolatilitySurfaceData data = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL,
        Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertEquals(X_LABEL, data.getXLabel());
    assertEquals(X_MIN, data.getXMinimum());
    assertEquals(X_MAX, data.getXMaximum());
    assertEquals(NX, data.getNXSamples());
    assertEquals(Y_LABEL, data.getYLabel());
    assertEquals(Y_MIN, data.getYMinimum());
    assertEquals(Y_MAX, data.getYMaximum());
    assertEquals(NY, data.getNYSamples());
    assertEquals(Z_MIN, data.getZMinimum());
    assertEquals(Z_MAX, data.getZMaximum());
    FunctionalVolatilitySurfaceData other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertEquals(data, other);
    final FunctionalDoublesSurface otherF = FunctionalDoublesSurface.from(new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return 3 * x[0] - 5 * x[1];
      }

      @Override
      public int hashCode() {
        return 1;
      }

      @Override
      public boolean equals(final Object o) {
        return true;
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(FunctionalDoublesSurface.class, "getParameterizedFunction");
      }

    }, "NAME2");
    final VolatilitySurface otherVol = new VolatilitySurface(otherF);
    other = new FunctionalVolatilitySurfaceData(otherVol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL + "a", X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN + 1, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX + 1, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX + 1, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL + 1, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN + 1, Y_MAX, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX + 1, NY, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY + 1, Z_MIN, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN + 1, Z_MAX);
    assertFalse(data.equals(other));
    other = new FunctionalVolatilitySurfaceData(vol, X_LABEL, X_MIN, X_MAX, NX, Y_LABEL, Y_MIN, Y_MAX, NY, Z_MIN, Z_MAX + 1);
    assertFalse(data.equals(other));
  }

  private static FunctionalDoublesSurface getSurface() {
    return FunctionalDoublesSurface.from(new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return 3 * x[0] - 5 * x[1];
      }

      @Override
      public int hashCode() {
        return 1;
      }

      @Override
      public boolean equals(final Object o) {
        return true;
      }

    }, "NAME1");
  }
}
