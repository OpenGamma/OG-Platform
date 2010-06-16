/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 */
public class InterpolationResultWithSensitivitiesTest {
  private static final double X = 1;
  private static final double[] DX = new double[] {2, 3, 4, 5, 6};

  @Test
  public void test() {
    final InterpolationResultWithSensitivities result1 = new InterpolationResultWithSensitivities(X, DX);
    final InterpolationResultWithSensitivities result2 = new InterpolationResultWithSensitivities(X, DX);
    assertEquals(result1.getResult(), X, 0);
    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
    assertFalse(result1.equals(new InterpolationResultWithSensitivities(X + 1, DX)));
    assertFalse(result1.equals(new InterpolationResultWithSensitivities(X, Arrays.copyOf(DX, DX.length + 10))));
  }
}
