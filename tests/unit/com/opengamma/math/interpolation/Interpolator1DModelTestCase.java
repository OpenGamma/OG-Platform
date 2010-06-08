/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public abstract class Interpolator1DModelTestCase {
  /**
   * 
   */
  private static final double EPS = 1e-10;
  private static final double[] KEYS1 = new double[] {5., 1., 4., 2., 3.};
  private static final double[] VALUES1 = new double[] {50., 10., 40., 20., 30.};
  
  protected abstract Interpolator1DModel createModel(double[] keys, double[] values);
  
  @Test
  public void size() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(5, model.size());
  }
  
  @Test
  public void lowerBounds() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(1., model.getLowerBoundKey(1.5), EPS);
    assertEquals(5., model.getLowerBoundKey(100.), EPS);
    assertNull(model.getLowerBoundKey(0.5));
  }
  
  @Test
  public void lowerBoundIndices() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);

    assertEquals(0, model.getLowerBoundIndex(1.5));
    assertEquals(4, model.getLowerBoundIndex(100.));
    assertEquals(3, model.getLowerBoundIndex(4.));
  }
  
  @Test
  public void firstKeyValue() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(1., model.firstKey(), EPS);
    assertEquals(10., model.firstValue(), EPS);
  }
  
  @Test
  public void lastKeyValue() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(5., model.lastKey(), EPS);
    assertEquals(50., model.lastValue(), EPS);
  }
  
  @Test
  public void higherKeyValue() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(1., model.higherKey(0.5), EPS);
    assertEquals(10., model.higherValue(0.5), EPS);
    assertEquals(5., model.higherKey(4.5), EPS);
    assertEquals(50., model.higherValue(4.5), EPS);
    assertNull(model.higherKey(5.5));
    assertNull(model.higherValue(5.5));
  }
  
  @Test
  public void pointLookup() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertEquals(10., model.get(1.), EPS);
    assertEquals(20., model.get(2.), EPS);
    assertEquals(30., model.get(3.), EPS);
    assertEquals(40., model.get(4.), EPS);
    assertEquals(50., model.get(5.), EPS);
    assertNull(model.get(4.5));
    assertNull(model.get(6.));
  }
  
  @Test
  public void containsKey() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    
    assertTrue(model.containsKey(1.));
    assertTrue(model.containsKey(2.));
    assertTrue(model.containsKey(3.));
    assertTrue(model.containsKey(4.));
    assertTrue(model.containsKey(5.));
    assertFalse(model.containsKey(1.5));
  }
  
  @Test
  public void keys() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    double[] keys = model.getKeys();
    assertEquals(1., keys[0], EPS);
    assertEquals(2., keys[1], EPS);
    assertEquals(3., keys[2], EPS);
    assertEquals(4., keys[3], EPS);
    assertEquals(5., keys[4], EPS);
    assertEquals(5, keys.length);
  }
  
  @Test
  public void values() {
    Interpolator1DModel model = createModel(KEYS1, VALUES1);
    double[] values = model.getValues();
    assertEquals(10., values[0], EPS);
    assertEquals(20., values[1], EPS);
    assertEquals(30., values[2], EPS);
    assertEquals(40., values[3], EPS);
    assertEquals(50., values[4], EPS);
    assertEquals(5, values.length);
  }
  
}
