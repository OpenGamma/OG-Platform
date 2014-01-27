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
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataTest {

  @Test
  public void testRectangular() {
    final Double[] xValues = new Double[] {1., 2., 3., 4., 5., 6., 7.};
    final Double[] yValues = new Double[] {100., 110., 120., 130., 140., 150., 160., 170., 180., 190.};
    final Double[] zValues = new Double[] {1100., 1110., 1120., 1130., 1140., 1150., 1160., 1170., 1180., 1190.};

    int xLength = xValues.length;
    int yLength = yValues.length;
    int zLength = zValues.length;
    final double[][][] vols = new double[yLength][xLength][zLength];
    final Double[] xs = new Double[xLength * yLength * zLength];
    final Double[] ys = new Double[xLength * yLength * zLength];
    final Double[] zs = new Double[xLength * yLength * zLength];
    final Map<Triple<Double, Double, Double>, Double> values = new HashMap<>();
    for (int i = 0, v = 0; i < xLength; i++) {
      for (int j = 0; j < yLength; j++) {
        for (int k = 0; k < zLength; k++, v++) {
          vols[k][j][i] = v;
          xs[v] = xValues[i];
          ys[v] = yValues[j];
          zs[v] = zValues[k];
          values.put(Triple.of(xValues[i], yValues[j], zValues[k]), vols[j][i][k]);
        }
      }
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilityCubeData<Double, Double, Double> data = new VolatilityCubeData<>(name, name, target, xs, ys, zs, values);
    final String xLabel = "time";
    final String yLabel = "strike";
    final String zLabel = "maturity";
    final VolatilityCubeData<Double, Double, Double> dataWithLabels = new VolatilityCubeData<>(name, name, target, xs, xLabel, ys, yLabel, zs, zLabel, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(zs, data.getZs());
    assertArrayEquals(xValues, data.getUniqueXValues().toArray(new Double[xLength]));
    assertArrayEquals(xs, dataWithLabels.getXs());
    assertArrayEquals(ys, dataWithLabels.getYs());
    assertArrayEquals(zs, dataWithLabels.getZs());
    assertArrayEquals(xValues, dataWithLabels.getUniqueXValues().toArray(new Double[xLength]));
    assertEquals("x", data.getXLabel());
    assertEquals("y", data.getYLabel());
    assertEquals("z", data.getZLabel());
    assertEquals(xLabel, dataWithLabels.getXLabel());
    assertEquals(yLabel, dataWithLabels.getYLabel());
    assertEquals(zLabel, dataWithLabels.getZLabel());
    int i = 0, j = 0;
    for (Double x : data.getUniqueXValues()) {
      for (Double y : data.getUniqueYValues()) {
        List<ObjectsPair<Double, Double>> strips = data.getZValuesForXandY(x, y);
        List<ObjectsPair<Double, Double>> stripsWithLabels = dataWithLabels.getZValuesForXandY(x, y);
        int k = 0;
        for (ObjectsPair<Double, Double> strip : strips) {
          assertEquals(zValues[k], strip.getFirst(), 0);
          assertEquals(vols[k++][j][i], strip.getSecond(), 0);
        }
        j++;
        assertEquals(strips, stripsWithLabels);
      }
      i++;
    }
  }
  
  @Test
  public void testRagged() {
    final Double[] xs = new Double[]   { 1.,  1.,  1.,  1.,  1.,  1.,  2.,  2.,  2.,   2.,   3.,   3.,   3.,   3.,   3.,   3.,   4.,   4.};
    final Double[] ys = new Double[]   { 4.,  5.,  6.,  4.,  5.,  6.,  4.,  5.,  4.,   5.,   5.,   6.,   7.,   5.,   6.,   7.,   8.,   9.};
    final Double[] zs = new Double[]   {14., 15., 16., 24., 25., 26., 14., 15., 24.,  25.,  15.,  16.,  17.,  25.,  26.,  27.,  18.,  28.};
    final double[] vols = new double[] {10., 11., 12., 13., 14., 15., 16., 17., 18., 210., 211., 212., 213., 214., 215., 216., 217., 218.};
    final Map<Triple<Double, Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Triple.of(xs[i], ys[i], zs[i]), vols[i]);
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilityCubeData<Double, Double, Double> data = new VolatilityCubeData<>(name, name, target, xs, ys, zs, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(zs, data.getZs());
    assertArrayEquals(new Double[]{1., 2., 3., 4.}, data.getUniqueXValues().toArray(new Double[3]));
    assertEquals(Arrays.asList(Pairs.of(14., 10.), Pairs.of(24., 13.)), data.getZValuesForXandY(1., 4.));
    assertEquals(Arrays.asList(Pairs.of(14., 16.), Pairs.of(24., 18.)), data.getZValuesForXandY(2., 4.));
    assertEquals(Arrays.asList(Pairs.of(25., 211.), Pairs.of(25., 214.)), data.getZValuesForXandY(3., 5.));
    assertEquals(Arrays.asList(Pairs.of(18., 217.)), data.getZValuesForXandY(4., 8.));
  }
}
