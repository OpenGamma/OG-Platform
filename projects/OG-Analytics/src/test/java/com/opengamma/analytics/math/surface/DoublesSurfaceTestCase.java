/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class DoublesSurfaceTestCase {
  static final String NAME = "a";
  static final double[] X_PRIMITIVE;
  static final double[] Y_PRIMITIVE;
  static final double[] Z_PRIMITIVE;
  static final Double[] X_OBJECT;
  static final Double[] Y_OBJECT;
  static final Double[] Z_OBJECT;
  static final List<Double> X_LIST;
  static final List<Double> Y_LIST;
  static final List<Double> Z_LIST;
  static final DoublesPair[] XY_PAIR;
  static final List<DoublesPair> XY_PAIR_LIST;
  static final Map<DoublesPair, Double> XYZ_MAP;
  static final List<Triple<Double, Double, Double>> XYZ_LIST;

  static {
    final int n = 10;
    X_PRIMITIVE = new double[n];
    Y_PRIMITIVE = new double[n];
    Z_PRIMITIVE = new double[n];
    X_OBJECT = new Double[n];
    Y_OBJECT = new Double[n];
    Z_OBJECT = new Double[n];
    X_LIST = new ArrayList<>();
    Y_LIST = new ArrayList<>();
    Z_LIST = new ArrayList<>();
    XY_PAIR = new DoublesPair[n];
    XY_PAIR_LIST = new ArrayList<>();
    XYZ_MAP = new LinkedHashMap<>();
    XYZ_LIST = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      final double x = i < 5 ? i : i - 5;
      final double y = i < 5 ? 0 : 1;
      final double z = 4 * x;
      final DoublesPair xy = DoublesPair.of(x, y);
      final Triple<Double, Double, Double> xyz = new Triple<>(x, y, z);
      X_PRIMITIVE[i] = x;
      Y_PRIMITIVE[i] = y;
      Z_PRIMITIVE[i] = z;
      X_OBJECT[i] = x;
      Y_OBJECT[i] = y;
      Z_OBJECT[i] = z;
      X_LIST.add(x);
      Y_LIST.add(y);
      Z_LIST.add(z);
      XY_PAIR[i] = xy;
      XY_PAIR_LIST.add(xy);
      XYZ_MAP.put(xy, z);
      XYZ_LIST.add(xyz);
    }
  }

  @Test
  public void testObjectArrays() {
    final DoublesSurface surface = new DummySurface(XYZ_LIST);
    final Double[] x = surface.getXData();
    assertTrue(x == surface.getXData());
    final Double[] y = surface.getYData();
    assertTrue(y == surface.getYData());
    final Double[] z = surface.getZData();
    assertTrue(z == surface.getZData());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    new DummySurface((double[]) null, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    new DummySurface(X_PRIMITIVE, (double[]) null, Z_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    new DummySurface(X_PRIMITIVE, Y_PRIMITIVE, (double[]) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    new DummySurface((Double[]) null, Y_OBJECT, Z_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    new DummySurface(X_OBJECT, (Double[]) null, Z_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    new DummySurface(X_OBJECT, Y_OBJECT, (Double[]) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull7() {
    new DummySurface((List<Double>) null, Y_LIST, Z_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull8() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(l, Y_LIST, Z_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull9() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(X_LIST, l, Z_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull10() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(X_LIST, Y_LIST, l, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull11() {
    new DummySurface((DoublesPair[]) null, X_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull12() {
    new DummySurface(XY_PAIR, (double[]) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull13() {
    new DummySurface(new DoublesPair[] {null}, Z_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull14() {
    new DummySurface((DoublesPair[]) null, Z_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull15() {
    new DummySurface(XY_PAIR, (Double[]) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull16() {
    new DummySurface(new DoublesPair[] {null}, Z_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull17() {
    new DummySurface((List<DoublesPair>) null, Z_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull18() {
    new DummySurface(XY_PAIR_LIST, (List<Double>) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull19() {
    new DummySurface(Arrays.asList(DoublesPair.of(1., 2.), null), Z_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull20() {
    new DummySurface(XY_PAIR_LIST, Arrays.asList(1., null), NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull21() {
    new DummySurface((Map<DoublesPair, Double>) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull22() {
    final Map<DoublesPair, Double> m = new HashMap<>();
    m.put(DoublesPair.of(1., 2.), 3.);
    m.put(null, 3.);
    new DummySurface(m, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull23() {
    final Map<DoublesPair, Double> m = new HashMap<>();
    m.put(DoublesPair.of(1., 2.), 3.);
    m.put(DoublesPair.of(1., 3.), null);
    new DummySurface(m, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull24() {
    new DummySurface((List<Triple<Double, Double, Double>>) null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull25() {
    final List<Triple<Double, Double, Double>> l = Arrays.asList(new Triple<>(1., 2., 3.), null);
    new DummySurface(l, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull26() {
    new DummySurface((double[]) null, Y_PRIMITIVE, Z_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull27() {
    new DummySurface(X_PRIMITIVE, (double[]) null, Z_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull28() {
    new DummySurface(X_PRIMITIVE, Y_PRIMITIVE, (double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull29() {
    new DummySurface((Double[]) null, Y_OBJECT, Z_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull30() {
    new DummySurface(X_OBJECT, (Double[]) null, Z_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull31() {
    new DummySurface(X_OBJECT, Y_OBJECT, (Double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull32() {
    new DummySurface((List<Double>) null, Y_LIST, Z_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull33() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(l, Y_LIST, Z_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull34() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(X_LIST, l, Z_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull35() {
    final List<Double> l = Arrays.asList(3., null);
    new DummySurface(X_LIST, Y_LIST, l);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull36() {
    new DummySurface((DoublesPair[]) null, X_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull37() {
    new DummySurface(XY_PAIR, (double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull38() {
    new DummySurface(new DoublesPair[] {null}, Z_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull39() {
    new DummySurface((DoublesPair[]) null, Z_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull40() {
    new DummySurface(XY_PAIR, (Double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull41() {
    new DummySurface(new DoublesPair[] {null}, Z_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull42() {
    new DummySurface((List<DoublesPair>) null, Z_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull43() {
    new DummySurface(XY_PAIR_LIST, (List<Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull44() {
    new DummySurface(Arrays.asList(DoublesPair.of(1., 2.), null), Z_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull45() {
    new DummySurface(XY_PAIR_LIST, Arrays.asList(1., null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull46() {
    new DummySurface((Map<DoublesPair, Double>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull47() {
    final Map<DoublesPair, Double> m = new HashMap<>();
    m.put(DoublesPair.of(1., 2.), 3.);
    m.put(null, 3.);
    new DummySurface(m);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull48() {
    final Map<DoublesPair, Double> m = new HashMap<>();
    m.put(DoublesPair.of(1., 2.), 3.);
    m.put(DoublesPair.of(1., 3.), null);
    new DummySurface(m);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull49() {
    new DummySurface((List<Triple<Double, Double, Double>>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull50() {
    final List<Triple<Double, Double, Double>> l = Arrays.asList(new Triple<>(1., 2., 3.), null);
    new DummySurface(l);
  }

  private static class DummySurface extends DoublesSurface {
    public DummySurface(final double[] xData, final double[] yData, final double[] zData) {
      super(xData, yData, zData);
    }

    public DummySurface(final Double[] xData, final Double[] yData, final Double[] zData) {
      super(xData, yData, zData);
    }

    public DummySurface(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
      super(xData, yData, zData);
    }

    public DummySurface(final DoublesPair[] xyData, final double[] zData) {
      super(xyData, zData);
    }

    public DummySurface(final DoublesPair[] xyData, final Double[] zData) {
      super(xyData, zData);
    }

    public DummySurface(final List<DoublesPair> xyData, final List<Double> zData) {
      super(xyData, zData);
    }

    public DummySurface(final Map<DoublesPair, Double> xyzData) {
      super(xyzData);
    }

    public DummySurface(final List<Triple<Double, Double, Double>> xyzData) {
      super(xyzData);
    }

    public DummySurface(final double[] xData, final double[] yData, final double[] zData, final String name) {
      super(xData, yData, zData, name);
    }

    public DummySurface(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
      super(xData, yData, zData, name);
    }

    public DummySurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
      super(xData, yData, zData, name);
    }

    public DummySurface(final DoublesPair[] xyData, final double[] zData, final String name) {
      super(xyData, zData, name);
    }

    public DummySurface(final DoublesPair[] xyData, final Double[] zData, final String name) {
      super(xyData, zData, name);
    }

    public DummySurface(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
      super(xyData, zData, name);
    }

    public DummySurface(final Map<DoublesPair, Double> xyzData, final String name) {
      super(xyzData, name);
    }

    public DummySurface(final List<Triple<Double, Double, Double>> xyzData, final String name) {
      super(xyzData, name);
    }

    @Override
    public Double getZValue(final Double x, final Double y) {
      return null;
    }

    @Override
    public Double getZValue(final Pair<Double, Double> xy) {
      return null;
    }
  }
}
