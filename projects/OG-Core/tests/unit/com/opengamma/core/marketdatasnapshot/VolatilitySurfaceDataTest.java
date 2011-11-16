/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
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
        values.put(DoublesPair.of(xValues[i], yValues[j]), vols[j][i]);
      }
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<Double, Double>(name, name, target, xs, ys, values);
    AssertJUnit.assertArrayEquals(xs, data.getXs());
    AssertJUnit.assertArrayEquals(ys, data.getYs());
    AssertJUnit.assertArrayEquals(xValues, data.getUniqueXValues().toArray(new Double[xLength]));
    int i = 0;
    for (Double x : data.getUniqueXValues()) {
      List<ObjectsPair<Double, Double>> strips = data.getYValuesForX(x);
      strips.size();
      int j = 0;
      for (ObjectsPair<Double, Double> strip : strips) {
        AssertJUnit.assertEquals(yValues[j], strip.getFirst(), 0);
        AssertJUnit.assertEquals(vols[j++][i], strip.getSecond(), 0);
      }
      i++;
    }
  }
  
  @Test
  public void testRagged() {
    final Double[] xs = new Double[] {1., 1., 1., 2., 2., 3., 3., 3., 4.};
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 5., 6., 7., 8.};
    final double[] vols = new double[] {10, 11, 12, 13, 14, 15, 16, 17, 18};    
    final Map<Pair<Double, Double>, Double> values = new HashMap<Pair<Double, Double>, Double>();
    for (int i = 0; i < xs.length; i++) {
      values.put(DoublesPair.of(xs[i], ys[i]), vols[i]);
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<Double, Double>(name, name, target, xs, ys, values);
    AssertJUnit.assertArrayEquals(xs, data.getXs());
    AssertJUnit.assertArrayEquals(ys, data.getYs());
    AssertJUnit.assertArrayEquals(new Double[]{1., 2., 3., 4.}, data.getUniqueXValues().toArray(new Double[3]));
    AssertJUnit.assertEquals(Arrays.asList(Pair.of(4., 10.), Pair.of(5., 11.), Pair.of(6., 12.)), data.getYValuesForX(1.));
    AssertJUnit.assertEquals(Arrays.asList(Pair.of(4., 13.), Pair.of(5., 14.)), data.getYValuesForX(2.));
    AssertJUnit.assertEquals(Arrays.asList(Pair.of(5., 15.), Pair.of(6., 16.), Pair.of(7., 17.)), data.getYValuesForX(3.));
    AssertJUnit.assertEquals(Arrays.asList(Pair.of(8., 18.)), data.getYValuesForX(4.));
  }
}
