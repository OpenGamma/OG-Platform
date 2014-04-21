/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PDEGreekResultCollectionTest {
  private static final double[] STRIKES = new double[] {1.1, 1.2, 1.3, 1.4};
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final double[] GRID_DELTA = new double[] {0.7, 0.75, 0.8, 0.85};
  private static final double[] GRID_GAMMA = new double[] {10, 11, 12, 13};
  private static final double[] GRID_VEGA = new double[] {0.1, 0.11, 0.12, 0.13};
  private static final PDEResultCollection RESULTS = new PDEResultCollection(STRIKES);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    new PDEResultCollection(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGreek() {
    RESULTS.put(null, GRID_DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    RESULTS.getPointGreek(PDEResultCollection.GRID_DELTA, 1.2, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength() {
    RESULTS.put(PDEResultCollection.GRID_DELTA, new double[] {1, 2, 3});
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableKeys() {
    final PDEResultCollection results = new PDEResultCollection(STRIKES);
    results.put(PDEResultCollection.GRID_BLACK_DELTA, GRID_DELTA);
    final Set<Greek> keys = results.keySet();
    keys.add(PDEResultCollection.GRID_BLACK_DELTA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValues() {
    final PDEResultCollection results = new PDEResultCollection(STRIKES);
    results.put(PDEResultCollection.GRID_BLACK_DELTA, GRID_DELTA);
    final Collection<double[]> values = results.values();
    values.add(GRID_VEGA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testRemove() {
    final PDEResultCollection results = new PDEResultCollection(STRIKES);
    final Iterator<Pair<Greek, double[]>> iter = results.iterator();
    iter.remove();
  }

  @Test
  public void testObject() {
    final PDEResultCollection results = new PDEResultCollection(STRIKES);
    assertArrayEquals(STRIKES, results.getStrikes(), 0);
    assertTrue(results.isEmpty());
    PDEResultCollection other = new PDEResultCollection(STRIKES);
    assertEquals(results, other);
    assertEquals(results.hashCode(), other.hashCode());
    other = new PDEResultCollection(new double[]  {1.1, 1.2, 1.3, 1.5});
    assertFalse(results.equals(other));
    other = new PDEResultCollection(STRIKES);
    results.put(PDEResultCollection.GRID_DELTA, GRID_DELTA);
    results.put(PDEResultCollection.GRID_VEGA, GRID_VEGA);
    results.put(PDEResultCollection.GRID_GAMMA, GRID_GAMMA);
    results.put(PDEResultCollection.GRID_VANNA, null);
    other.put(PDEResultCollection.GRID_DELTA, GRID_DELTA);
    other.put(PDEResultCollection.GRID_VEGA, GRID_VEGA);
    other.put(PDEResultCollection.GRID_GAMMA, GRID_GAMMA);
    assertFalse(results.equals(other));
    other.put(PDEResultCollection.GRID_VANNA, null);
    assertEquals(results, other);
    assertEquals(results.hashCode(), other.hashCode());
    assertEquals(4, results.size());
    assertTrue(results.contains(PDEResultCollection.GRID_DELTA));
    assertTrue(results.contains(PDEResultCollection.GRID_VANNA));
    assertFalse(results.contains(PDEResultCollection.GRID_BLACK_DELTA));
    assertArrayEquals(results.getGridGreeks(PDEResultCollection.GRID_DELTA), GRID_DELTA, 0);
    assertArrayEquals(results.getGridGreeks(PDEResultCollection.GRID_GAMMA), GRID_GAMMA, 0);
    assertArrayEquals(results.getGridGreeks(PDEResultCollection.GRID_VEGA), GRID_VEGA, 0);
    assertNull(results.getGridGreeks(PDEResultCollection.GRID_VANNA));
    assertNull(results.getGridGreeks(PDEResultCollection.GRID_VOMMA));
    assertEquals(results.getPointGreek(PDEResultCollection.GRID_DELTA, 1.16, INTERPOLATOR), 0.73, EPS);
    assertEquals(results.getPointGreek(PDEResultCollection.GRID_GAMMA, 1.27, INTERPOLATOR), 11.7, EPS);
    assertEquals(results.getPointGreek(PDEResultCollection.GRID_VEGA, 1.39, INTERPOLATOR), 0.129, EPS);
    assertNull(results.getPointGreek(PDEResultCollection.GRID_VANNA, 1.5, INTERPOLATOR));
    assertNull(results.getPointGreek(PDEResultCollection.GRID_VOMMA, 1.5, INTERPOLATOR));
    final Iterator<Pair<Greek, double[]>> resultIter = results.iterator();
    final Iterator<Greek> dataIter = results.keySet().iterator();
    while (resultIter.hasNext()) {
      final Pair<Greek, double[]> pair = resultIter.next();
      assertTrue(results.keySet().contains(pair.getFirst()));
      assertEquals(pair.getFirst(), dataIter.next());
    }
    assertFalse(dataIter.hasNext());
  }
}
