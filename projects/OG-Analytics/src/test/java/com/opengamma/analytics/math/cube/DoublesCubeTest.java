/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoublesCubeTest {
  static final String NAME = "a";
  static final double[] X_PRIMITIVE;
  static final double[] Y_PRIMITIVE;
  static final double[] Z_PRIMITIVE;
  static final double[] DATA_PRIMITIVE;
  static final Double[] X_OBJECT;
  static final Double[] Y_OBJECT;
  static final Double[] Z_OBJECT;
  static final Double[] DATA_OBJECT;
  static final List<Double> X_LIST;
  static final List<Double> Y_LIST;
  static final List<Double> Z_LIST;
  static final List<Double> DATA_LIST;

  static {
    final int n = 125;
    X_PRIMITIVE = new double[n];
    Y_PRIMITIVE = new double[n];
    Z_PRIMITIVE = new double[n];
    DATA_PRIMITIVE = new double[n];
    X_OBJECT = new Double[n];
    Y_OBJECT = new Double[n];
    Z_OBJECT = new Double[n];
    DATA_OBJECT = new Double[n];
    X_LIST = new ArrayList<>();
    Y_LIST = new ArrayList<>();
    Z_LIST = new ArrayList<>();
    DATA_LIST = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < 5; i++) {
      final double x = i;
      for (int j = 0; j < 5; j++) {
        final double y = j;
        for (int k = 0; k < 5; k++) {
          final double z = k;
          final double data = k;
          X_PRIMITIVE[count] = x;
          Y_PRIMITIVE[count] = y;
          Z_PRIMITIVE[count] = z;
          DATA_PRIMITIVE[count] = data;
          X_OBJECT[count] = x;
          Y_OBJECT[count] = y;
          Z_OBJECT[count] = z;
          DATA_OBJECT[count] = data;
          X_LIST.add(x);
          Y_LIST.add(y);
          Z_LIST.add(z);
          DATA_LIST.add(data);
          count++;
        }
      }
    }
  }

  @Test
  public void testObjectArrays() {
    final DoublesCube cube = new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE);
    final Double[] x = cube.getXData();
    assertTrue(x == cube.getXData());
    final Double[] y = cube.getYData();
    assertTrue(y == cube.getYData());
    final Double[] z = cube.getZData();
    assertTrue(z == cube.getZData());
    final Double[] data = cube.getValues();
    assertTrue(data == cube.getValues());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    new DummyCube(null, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    new DummyCube(X_PRIMITIVE, null, Z_PRIMITIVE, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, null, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength1() {
    new DummyCube(new double[] {2}, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength2() {
    new DummyCube(X_PRIMITIVE, new double[] {2}, Z_PRIMITIVE, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength3() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, new double[] {2}, DATA_PRIMITIVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength4() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, new double[] {2});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    new DummyCube(null, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    new DummyCube(X_PRIMITIVE, null, Z_PRIMITIVE, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull7() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, null, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull8() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength5() {
    new DummyCube(new double[] {2}, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength6() {
    new DummyCube(X_PRIMITIVE, new double[] {2}, Z_PRIMITIVE, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength7() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, new double[] {2}, DATA_PRIMITIVE, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength8() {
    new DummyCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, new double[] {2}, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull9() {
    new DummyCube(null, Y_OBJECT, Z_OBJECT, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull10() {
    new DummyCube(X_OBJECT, null, Z_OBJECT, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull11() {
    new DummyCube(X_OBJECT, Y_OBJECT, null, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull12() {
    new DummyCube(X_OBJECT, Y_OBJECT, Z_OBJECT, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength9() {
    new DummyCube(new Double[] {2.}, Y_OBJECT, Z_OBJECT, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength10() {
    new DummyCube(X_OBJECT, new Double[] {2.}, Z_OBJECT, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength11() {
    new DummyCube(X_OBJECT, Y_OBJECT, new Double[] {2.}, DATA_OBJECT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength12() {
    new DummyCube(X_OBJECT, Y_OBJECT, Z_OBJECT, new Double[] {2.});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull13() {
    new DummyCube(null, Y_OBJECT, Z_OBJECT, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull14() {
    new DummyCube(X_OBJECT, null, Z_OBJECT, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull15() {
    new DummyCube(X_OBJECT, Y_OBJECT, null, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull16() {
    new DummyCube(X_OBJECT, Y_OBJECT, Z_OBJECT, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength13() {
    new DummyCube(new Double[] {2.}, Y_OBJECT, Z_OBJECT, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength14() {
    new DummyCube(X_OBJECT, new Double[] {2.}, Z_OBJECT, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength15() {
    new DummyCube(X_OBJECT, Y_OBJECT, new Double[] {2.}, DATA_OBJECT, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength16() {
    new DummyCube(X_OBJECT, Y_OBJECT, Z_OBJECT, new Double[] {2.}, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull17() {
    new DummyCube(null, Y_LIST, Z_LIST, DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull18() {
    new DummyCube(X_LIST, null, Z_LIST, DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull19() {
    new DummyCube(X_LIST, Y_LIST, null, DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull20() {
    new DummyCube(X_LIST, Y_LIST, Z_LIST, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength17() {
    new DummyCube(Arrays.asList(2.), Y_LIST, Z_LIST, DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength18() {
    new DummyCube(X_LIST, Arrays.asList(2.), Z_LIST, DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength19() {
    new DummyCube(X_LIST, Y_LIST, Arrays.asList(2.), DATA_LIST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength20() {
    new DummyCube(X_LIST, Y_LIST, Z_LIST, Arrays.asList(2.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull21() {
    new DummyCube(null, Y_LIST, Z_LIST, DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull22() {
    new DummyCube(X_LIST, null, Z_LIST, DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull23() {
    new DummyCube(X_LIST, Y_LIST, null, DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull24() {
    new DummyCube(X_LIST, Y_LIST, Z_LIST, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength21() {
    new DummyCube(Arrays.asList(2.), Y_LIST, Z_LIST, DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength22() {
    new DummyCube(X_LIST, Arrays.asList(2.), Z_LIST, DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength23() {
    new DummyCube(X_LIST, Y_LIST, Arrays.asList(2.), DATA_LIST, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength24() {
    new DummyCube(X_LIST, Y_LIST, Z_LIST, Arrays.asList(2.), NAME);
  }

  private static class DummyCube extends DoublesCube {
    public DummyCube(final double[] xData, final double[] yData, final double[] zData, final double[] data) {
      super(xData, yData, zData, data);
    }

    public DummyCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] data) {
      super(xData, yData, zData, data);
    }

    public DummyCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> data) {
      super(xData, yData, zData, data);
    }

    public DummyCube(final double[] xData, final double[] yData, final double[] zData, final double[] data, final String name) {
      super(xData, yData, zData, data, name);
    }

    public DummyCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] data, final String name) {
      super(xData, yData, zData, data, name);
    }

    public DummyCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> data, final String name) {
      super(xData, yData, zData, data, name);
    }

    @Override
    public Double getValue(final Double x, final Double y, final Double z) {
      return null;
    }

    @Override
    public Double getValue(final Triple<Double, Double, Double> xyz) {
      return null;
    }
  }

}
