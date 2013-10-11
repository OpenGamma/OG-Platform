/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceDataTest {

  @Test
  public void testRectangular() {
    final Double[] xValues = new Double[] {1., 2., 3., 4., 5., 6., 7.};
    final Double[] yValues = new Double[] {100., 110., 120., 130., 140., 150., 160., 170., 180., 190.};
    int xLength = xValues.length;
    int yLength = yValues.length;
    final double[][] vols = new double[yLength][xLength];    
    final Double[] xs = new Double[xLength * yLength];
    final Double[] ys = new Double[xLength * yLength];
    final Map<Pair<Double, Double>, Double> values = new HashMap<Pair<Double, Double>, Double>();
    for (int i = 0, k = 0; i < xLength; i++) {
      for (int j = 0; j < yLength; j++, k++) {
        vols[j][i] = k;
        xs[k] = xValues[i];
        ys[k] = yValues[j];
        values.put(Pairs.of(xValues[i], yValues[j]), vols[j][i]);
      }
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<Double, Double>(name, name, target, xs, ys, values);
    final String xLabel = "time";
    final String yLabel = "strike";
    final VolatilitySurfaceData<Double, Double> dataWithLabels = new VolatilitySurfaceData<Double, Double>(name, name, target, xs, xLabel, ys, yLabel, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(xValues, data.getUniqueXValues().toArray(new Double[xLength]));
    assertArrayEquals(xs, dataWithLabels.getXs());
    assertArrayEquals(ys, dataWithLabels.getYs());
    assertArrayEquals(xValues, dataWithLabels.getUniqueXValues().toArray(new Double[xLength]));
    assertEquals("x", data.getXLabel());
    assertEquals("y", data.getYLabel());
    assertEquals(xLabel, dataWithLabels.getXLabel());
    assertEquals(yLabel, dataWithLabels.getYLabel());
    int i = 0;
    for (Double x : data.getUniqueXValues()) {
      List<ObjectsPair<Double, Double>> strips = data.getYValuesForX(x);
      List<ObjectsPair<Double, Double>> stripsWithLabels = dataWithLabels.getYValuesForX(x);
      int j = 0;
      for (ObjectsPair<Double, Double> strip : strips) {
        assertEquals(yValues[j], strip.getFirst(), 0);
        assertEquals(vols[j++][i], strip.getSecond(), 0);
      }
      i++;
      assertEquals(strips, stripsWithLabels);
    }
  }
  
  @Test
  public void testRagged() {
    final Double[] xs = new Double[] {1., 1., 1., 2., 2., 3., 3., 3., 4.};
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 5., 6., 7., 8.};
    final double[] vols = new double[] {10, 11, 12, 13, 14, 15, 16, 17, 18};    
    final Map<Pair<Double, Double>, Double> values = new HashMap<Pair<Double, Double>, Double>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Pairs.of(xs[i], ys[i]), vols[i]);
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<Double, Double>(name, name, target, xs, ys, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(new Double[]{1., 2., 3., 4.}, data.getUniqueXValues().toArray(new Double[3]));
    assertEquals(Arrays.asList(Pairs.of(4., 10.), Pairs.of(5., 11.), Pairs.of(6., 12.)), data.getYValuesForX(1.));
    assertEquals(Arrays.asList(Pairs.of(4., 13.), Pairs.of(5., 14.)), data.getYValuesForX(2.));
    assertEquals(Arrays.asList(Pairs.of(5., 15.), Pairs.of(6., 16.), Pairs.of(7., 17.)), data.getYValuesForX(3.));
    assertEquals(Arrays.asList(Pairs.of(8., 18.)), data.getYValuesForX(4.));
  }
}
