/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AffineDividendsTest {

  final static double[] TAU0 = new double[0];
  final static double[] TAU1 = new double[] {0.34 };
  final static double[] TAU2 = new double[] {0.34, 1.23 };
  final static double[] ALPHA0 = new double[0];
  final static double[] ALPHA1 = new double[] {12 };
  final static double[] ALPHA2 = new double[] {5, 2 };
  final static double[] BETA0 = new double[0];
  final static double[] BETA1 = new double[] {0.2 };
  final static double[] BETA2 = new double[] {0.1, 0.05 };

  @Test
  public void zeroLengthTest() {
    final AffineDividends div = new AffineDividends(TAU0, ALPHA0, BETA0);
    assertEquals(0, div.getNumberOfDividends());
  }

  @Test
  public void unitLengthTest() {
    final AffineDividends div = new AffineDividends(TAU1, ALPHA1, BETA1);
    assertEquals(1, div.getNumberOfDividends());
    assertEquals(TAU1[0], div.getTau(0));
    assertEquals(ALPHA1[0], div.getAlpha(0));
    assertEquals(BETA1[0], div.getBeta(0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTest1() {
    new AffineDividends(null, ALPHA1, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTest2() {
    new AffineDividends(TAU1, null, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTest3() {
    new AffineDividends(TAU1, ALPHA1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthTest1() {
    new AffineDividends(TAU2, ALPHA1, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthTest2() {
    new AffineDividends(TAU1, ALPHA2, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthTest3() {
    new AffineDividends(TAU1, ALPHA1, BETA2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negTauTest() {
    new AffineDividends(new double[] {-1 }, ALPHA1, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nonAssendingTauTest() {
    new AffineDividends(new double[] {1.2, 0.9 }, ALPHA2, BETA2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negAlphaTest() {
    new AffineDividends(TAU1, new double[] {-2 }, BETA1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negBetaTest() {
    new AffineDividends(TAU1, ALPHA1, new double[] {-0.02 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeBetaTest() {
    new AffineDividends(TAU1, ALPHA1, new double[] {1.02 });
  }

  @Test
  public void testObject() {
    final AffineDividends dividends = new AffineDividends(TAU1, ALPHA1, BETA1);
    AffineDividends other = new AffineDividends(TAU1, ALPHA1, BETA1);
    assertEquals(dividends, other);
    assertEquals(dividends.hashCode(), other.hashCode());
    other = new AffineDividends(new double[] {0.4}, ALPHA1, BETA1);
    assertFalse(dividends.equals(other));
    other = new AffineDividends(TAU1, new double[] {0.2345}, BETA1);
    assertFalse(dividends.equals(other));
    other = new AffineDividends(TAU1, ALPHA1, new double[] {0.123456});
    assertFalse(dividends.equals(other));
  }
}
