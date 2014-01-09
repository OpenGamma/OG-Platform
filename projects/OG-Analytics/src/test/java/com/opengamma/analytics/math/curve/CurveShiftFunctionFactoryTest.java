/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveShiftFunctionFactoryTest {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x;
    }

  };
  private static final ConstantDoublesCurve CONSTANT = ConstantDoublesCurve.from(3.4);
  private static final FunctionalDoublesCurve FUNCTIONAL = FunctionalDoublesCurve.from(F);
  private static final InterpolatedDoublesCurve INTERPOLATED = InterpolatedDoublesCurve.from(new double[] {1, 2 }, new double[] {1.2, 3.4 }, new LinearInterpolator1D());
  private static final SpreadDoublesCurve SPREAD = SpreadDoublesCurve.from(new AddCurveSpreadFunction(), new DoublesCurve[] {INTERPOLATED, CONSTANT });
  private static final DoublesCurve DUMMY = new DoublesCurve() {

    @Override
    public Double[] getXData() {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public Double[] getYData() {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Double getYValue(final Double x) {
      return 0.;
    }

    @Override
    public Double[] getYValueParameterSensitivity(final Double x) {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public double getDyDx(final double x) {
      return 0;
    }

  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongClass() {
    CurveShiftFunctionFactory.getFunction(String.class);
  }

  @Test
  public void testFunction() {
    assertEquals(ConstantCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(ConstantCurveShiftFunction.class).getClass());
    assertEquals(FunctionalCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(FunctionalCurveShiftFunction.class).getClass());
    assertEquals(InterpolatedCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(InterpolatedCurveShiftFunction.class).getClass());
    assertEquals(SpreadCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(SpreadCurveShiftFunction.class).getClass());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType1() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType2() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, 1, "M");
  }

  @Test
  public void testGetShiftedCurve1() {
    final double shift = 2;
    Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, shift);
    Curve<Double, Double> expected = new ConstantCurveShiftFunction().evaluate(CONSTANT, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getYValue(0.), expected.getYValue(0.));
    shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, shift);
    expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesCurve) shifted).getInterpolator(), ((InterpolatedDoublesCurve) expected).getInterpolator());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    shifted = CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, shift);
    expected = new FunctionalCurveShiftFunction().evaluate(FUNCTIONAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getYValue(0.), expected.getYValue(0.));
    shifted = CurveShiftFunctionFactory.getShiftedCurve(SPREAD, shift);
    expected = new SpreadCurveShiftFunction().evaluate(SPREAD, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getYValue(1.5), expected.getYValue(1.5));

    final String newName = "F";
    shifted = CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, shift, newName);
    expected = new ConstantCurveShiftFunction().evaluate(CONSTANT, shift, newName);
    assertEquals(expected, shifted);
    shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, shift, newName);
    expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, shift, newName);
    assertEquals(expected, shifted);
    shifted = CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, shift, newName);
    expected = new FunctionalCurveShiftFunction().evaluate(FUNCTIONAL, shift, newName);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getYValue(0.), expected.getYValue(0.));
    assertEquals(shifted.getName(), expected.getName());
    shifted = CurveShiftFunctionFactory.getShiftedCurve(SPREAD, shift, newName);
    expected = new SpreadCurveShiftFunction().evaluate(SPREAD, shift, newName);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getYValue(1.5), expected.getYValue(1.5));
    assertEquals(shifted.getName(), expected.getName());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType3() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, 1, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType4() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, 1, 1, "J");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift1() {
    CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, 1, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift2() {
    CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, 1, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift3() {
    CurveShiftFunctionFactory.getShiftedCurve(SPREAD, 1, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift4() {
    CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, 1, 1, "L");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift5() {
    CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, 1, 1, "P");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift6() {
    CurveShiftFunctionFactory.getShiftedCurve(SPREAD, 1, 1, "O");
  }

  @Test
  public void testGetShiftedCurve2() {
    final double x = 1;
    final double shift = 2;
    Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, x, shift);
    Curve<Double, Double> expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, x, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesCurve) shifted).getInterpolator(), ((InterpolatedDoublesCurve) expected).getInterpolator());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, x, shift, newName);
    expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, x, shift, newName);
    assertEquals(shifted, expected);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType5() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, new double[] {1 }, new double[] {1 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType6() {
    CurveShiftFunctionFactory.getShiftedCurve(DUMMY, new double[] {1 }, new double[] {1 }, "N");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift7() {
    CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, new double[] {1 }, new double[] {1 });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift8() {
    CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, new double[] {1 }, new double[] {1 });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift9() {
    CurveShiftFunctionFactory.getShiftedCurve(SPREAD, new double[] {1 }, new double[] {1 });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift10() {
    CurveShiftFunctionFactory.getShiftedCurve(CONSTANT, new double[] {1 }, new double[] {1 }, "L");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift11() {
    CurveShiftFunctionFactory.getShiftedCurve(FUNCTIONAL, new double[] {1 }, new double[] {1 }, "K");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnsupportedShift12() {
    CurveShiftFunctionFactory.getShiftedCurve(SPREAD, new double[] {1 }, new double[] {1 }, "J");
  }

  @Test
  public void testGetShiftedCurve3() {
    final double[] x = new double[] {1 };
    final double[] y = new double[] {2 };
    Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, x, y);
    Curve<Double, Double> expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, x, y);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesCurve) shifted).getInterpolator(), ((InterpolatedDoublesCurve) expected).getInterpolator());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "M";
    shifted = CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED, x, y, newName);
    expected = new InterpolatedCurveShiftFunction().evaluate(INTERPOLATED, x, y, newName);
    assertEquals(shifted, expected);
    assertEquals(((InterpolatedDoublesCurve) shifted).getInterpolator(), ((InterpolatedDoublesCurve) expected).getInterpolator());
  }

}
