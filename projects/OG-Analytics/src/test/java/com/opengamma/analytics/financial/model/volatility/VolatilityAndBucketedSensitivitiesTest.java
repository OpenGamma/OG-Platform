/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityAndBucketedSensitivitiesTest {
  private static final double VOL = 0.34;
  private static final double[][] SENSITIVITIES = new double[][] {new double[] {0.1, 0.2, 0.3}, new double[] {0.4, 0.5, 0.6}};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivities() {
    new VolatilityAndBucketedSensitivities(VOL, null);
  }

  @Test
  public void test() {
    final VolatilityAndBucketedSensitivities object = new VolatilityAndBucketedSensitivities(VOL, SENSITIVITIES);
    assertEquals(VOL, object.getVolatility());
    assertArrayEquals(SENSITIVITIES, object.getBucketedSensitivities());
    VolatilityAndBucketedSensitivities other = new VolatilityAndBucketedSensitivities(VOL, new double[][] {new double[] {0.1, 0.2, 0.3}, new double[] {0.4, 0.5, 0.6}});
    assertEquals(object, other);
    assertEquals(object.hashCode(), other.hashCode());
    other = new VolatilityAndBucketedSensitivities(VOL + 0.01, SENSITIVITIES);
    assertFalse(other.equals(object));
    other = new VolatilityAndBucketedSensitivities(VOL, new double[][] {SENSITIVITIES[0]});
    assertFalse(other.equals(object));
  }
}
