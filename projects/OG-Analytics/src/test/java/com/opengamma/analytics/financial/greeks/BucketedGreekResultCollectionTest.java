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

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BucketedGreekResultCollectionTest {
  private static final double[] EXPIRIES = new double[] {1, 5, 10, 20};
  private static final double[][] STRIKES = new double[][] {
    new double[] {1.1, 1.2, 1.3},
    new double[] {1.1, 1.2, 1.3},
    new double[] {1.1, 1.2},
    new double[] {1.1, 1.2, 1.3}
  };
  private static final double[][] VEGA = new double[][] {
    new double[] {0.1, 0.2, 0.3},
    new double[] {10.1, 10.2, 10.3},
    new double[] {20.1, 20.2},
    new double[] {30.1, 30.2, 30.3}
  };
  private static final BucketedGreekResultCollection RESULTS = new BucketedGreekResultCollection(EXPIRIES, STRIKES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    new BucketedGreekResultCollection(EXPIRIES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries() {
    new BucketedGreekResultCollection(null, STRIKES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPutNullGreek() {
    RESULTS.put(null, VEGA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStrikeNumber1() {
    final double[][] result = new double[][] {
        new double[] {0.1, 0.2, 0.3},
    };
    RESULTS.put(BucketedGreekResultCollection.BUCKETED_VEGA, result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStrikeNumber2() {
    final double[][] result = new double[][] {
        new double[] {0.1, 0.2, 0.3},
        new double[] {10.1, 10.2, 10.3},
        new double[] {20.1, 20.2, 20.3},
        new double[] {30.1, 30.2, 30.3}
    };
    RESULTS.put(BucketedGreekResultCollection.BUCKETED_VEGA, result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongExpiryNumber() {
    final double[][] result = new double[][] {
        new double[] {0.1, 0.2, 0.3, 0.4},
        new double[] {10.1, 10.2, 10.3, 10.4},
        new double[] {20.1, 20.2, 20.3, 20.4},
        new double[] {30.1, 30.2, 30.3, 30.4}
    };
    RESULTS.put(BucketedGreekResultCollection.BUCKETED_VEGA, result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonRectangular() {
    final double[][] result = new double[][] {
        new double[] {0.1, 0.2, 0.3},
        new double[] {10.1, 10.2},
        new double[] {20.1, 20.2, 20.3},
        new double[] {30.1, 30.2, 30.3}
    };
    RESULTS.put(BucketedGreekResultCollection.BUCKETED_VEGA, result);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableKeys() {
    final BucketedGreekResultCollection results = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    results.put(BucketedGreekResultCollection.BUCKETED_VEGA, VEGA);
    final Set<Greek> keys = results.keySet();
    keys.add(BucketedGreekResultCollection.BUCKETED_VEGA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValues() {
    final BucketedGreekResultCollection results = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    results.put(BucketedGreekResultCollection.BUCKETED_VEGA, VEGA);
    final Collection<double[][]> values = results.values();
    values.add(VEGA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testRemove() {
    final BucketedGreekResultCollection results = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    final Iterator<Pair<Greek, double[][]>> iter = results.iterator();
    iter.remove();
  }

  @Test
  public void testObject() {
    final BucketedGreekResultCollection results = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    for (int i = 0; i < STRIKES.length; i++) {
      assertArrayEquals(STRIKES[i], results.getStrikes()[i], 0);
    }
    assertArrayEquals(EXPIRIES, results.getExpiries(), 0);
    BucketedGreekResultCollection other = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    assertEquals(results, other);
    assertEquals(results.hashCode(), other.hashCode());
    final double[][] strikes = new double[][] {
        new double[] {1.1, 1.2, 1.4},
        new double[] {1.1, 1.2, 1.3},
        new double[] {1.1, 1.2},
        new double[] {1.1, 1.2, 1.3}
    };
    other = new BucketedGreekResultCollection(EXPIRIES, strikes);
    assertFalse(results.equals(other));
    other = new BucketedGreekResultCollection(new double[] {1, 5, 10, 15}, STRIKES);
    assertFalse(results.equals(other));
    other = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    results.put(BucketedGreekResultCollection.BUCKETED_VEGA, VEGA);
    assertFalse(results.equals(other));
    other.put(BucketedGreekResultCollection.BUCKETED_VEGA, VEGA);
    assertEquals(results, other);
    assertEquals(results.hashCode(), other.hashCode());
    assertTrue(results.contains(BucketedGreekResultCollection.BUCKETED_VEGA));
    assertEquals(1, results.size());
    for (int i = 0; i < VEGA.length; i++) {
      assertArrayEquals(VEGA[i], results.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA)[i], 0);
    }
    assertNull(results.getBucketedGreeks(Greek.CARRY_RHO));
    final Iterator<Pair<Greek, double[][]>> resultIter = results.iterator();
    final Iterator<Greek> dataIter = results.keySet().iterator();
    while (resultIter.hasNext()) {
      final Pair<Greek, double[][]> pair = resultIter.next();
      assertTrue(results.keySet().contains(pair.getFirst()));
      assertEquals(pair.getFirst(), dataIter.next());
    }
    assertFalse(dataIter.hasNext());
  }
}
