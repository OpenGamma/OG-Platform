/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory.getFunction;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SurfaceShiftFunctionFactoryTest {
  private static final Function<Double, Double> F = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      return xy[0];
    }

  };
  private static final Surface<Double, Double, Double> DUMMY = new Surface<Double, Double, Double>() {

    @Override
    public Double[] getXData() {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public Double[] getYData() {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public Double[] getZData() {
      return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Double getZValue(final Double x, final Double y) {
      return 0.;
    }

    @Override
    public Double getZValue(final Pair<Double, Double> xy) {
      return 0.;
    }

  };
  private static final ConstantDoublesSurface CONSTANT = ConstantDoublesSurface.from(3.4);
  private static final FunctionalDoublesSurface FUNCTIONAL = FunctionalDoublesSurface.from(F);
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final InterpolatedDoublesSurface INTERPOLATED = InterpolatedDoublesSurface.from(new double[] {1, 2, 1, 2}, new double[] {1, 2, 3, 4}, new double[] {1.2, 3.4, 5.6, 7.8},
      new GridInterpolator2D(LINEAR, LINEAR));
  @SuppressWarnings("unchecked")
  private static final InterpolatedFromCurvesDoublesSurface INTERPOLATED_FROM_CURVES = InterpolatedFromCurvesDoublesSurface.from(true, new double[] {1},
      new Curve[] {InterpolatedDoublesCurve.from(new double[] {1, 2}, new double[] {3, 4}, LINEAR)}, LINEAR);
  private static final NodalDoublesSurface NODAL = NodalDoublesSurface.from(new double[] {1, 2}, new double[] {1, 2}, new double[] {1.2, 3.4});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongClass() {
    SurfaceShiftFunctionFactory.getFunction(Double.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType1() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, .2, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType2() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, 1, 2, 3, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType3() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, new double[] {1}, new double[] {2}, new double[] {3}, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType4() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, .2, "N", false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType5() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, 1, 2, 3, "N", false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType6() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, new double[] {1}, new double[] {2}, new double[] {3}, "N", false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetMultiplicativeInterpolatedDoublesFromCurveSurface() {
    SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, 1, 1, 2, "M", false);
  }

  @Test
  public void testGetFunction() {
    assertEquals(ConstantSurfaceAdditiveShiftFunction.class, getFunction(ConstantSurfaceAdditiveShiftFunction.class).getClass());
    assertEquals(FunctionalSurfaceAdditiveShiftFunction.class, getFunction(FunctionalSurfaceAdditiveShiftFunction.class).getClass());
    assertEquals(InterpolatedSurfaceAdditiveShiftFunction.class, getFunction(InterpolatedSurfaceAdditiveShiftFunction.class).getClass());
    assertEquals(InterpolatedFromCurvesSurfaceAdditiveShiftFunction.class, getFunction(InterpolatedFromCurvesSurfaceAdditiveShiftFunction.class).getClass());
    assertEquals(NodalSurfaceAdditiveShiftFunction.class, getFunction(NodalSurfaceAdditiveShiftFunction.class).getClass());
    assertEquals(ConstantSurfaceMultiplicativeShiftFunction.class, getFunction(ConstantSurfaceMultiplicativeShiftFunction.class).getClass());
    assertEquals(FunctionalSurfaceMultiplicativeShiftFunction.class, getFunction(FunctionalSurfaceMultiplicativeShiftFunction.class).getClass());
    assertEquals(InterpolatedSurfaceMultiplicativeShiftFunction.class, getFunction(InterpolatedSurfaceMultiplicativeShiftFunction.class).getClass());
    assertEquals(NodalSurfaceMultiplicativeShiftFunction.class, getFunction(NodalSurfaceMultiplicativeShiftFunction.class).getClass());
  }

  @Test
  public void testGetAdditiveShiftedSurface1() {
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift, true);
    Surface<Double, Double, Double> expected = new ConstantSurfaceAdditiveShiftFunction().evaluate(CONSTANT, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.), 0);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift, true);
    expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, shift, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift, true);
    expected = new FunctionalSurfaceAdditiveShiftFunction().evaluate(FUNCTIONAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));

    final String newName = "F";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift, newName, true);
    expected = new ConstantSurfaceAdditiveShiftFunction().evaluate(CONSTANT, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift, newName, true);
    expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, shift, newName, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift, newName, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift, newName, true);
    expected = new FunctionalSurfaceAdditiveShiftFunction().evaluate(FUNCTIONAL, shift, newName);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));
    assertEquals(shifted.getName(), expected.getName());
  }

  @Test
  public void testGetMultiplicativeShiftedSurface1() {
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift, false);
    Surface<Double, Double, Double> expected = new ConstantSurfaceMultiplicativeShiftFunction().evaluate(CONSTANT, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.), 0);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift, false);
    expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift, false);
    expected = new FunctionalSurfaceMultiplicativeShiftFunction().evaluate(FUNCTIONAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));

    final String newName = "F";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift, newName, false);
    expected = new ConstantSurfaceMultiplicativeShiftFunction().evaluate(CONSTANT, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift, newName, false);
    expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift, newName, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift, newName, false);
    expected = new FunctionalSurfaceMultiplicativeShiftFunction().evaluate(FUNCTIONAL, shift, newName);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));
    assertEquals(shifted.getName(), expected.getName());
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported1() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2, true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported2() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2, "M", true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported3() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2, true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported4() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2, "M", true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported5() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2, false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported6() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2, "M", false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported7() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2, false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported8() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2, "M", false);
  }

  @Test
  public void testGetAdditiveShiftedSurface2() {
    final double x = 1;
    final double y = 1;
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, true);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName, true);
    expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, newName, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }

  @Test
  public void testGetMultiplicativeShiftedSurface2() {
    final double x = 1;
    final double y = 1;
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, false);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName, false);
    expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported9() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1}, new double[] {1}, new double[] {2}, true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported10() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1}, new double[] {1}, new double[] {2}, "M", true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported11() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1}, new double[] {1}, new double[] {2}, true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported12() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1}, new double[] {1}, new double[] {2}, "M", true);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported13() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1}, new double[] {1}, new double[] {2}, false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported14() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1}, new double[] {1}, new double[] {2}, "M", false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported15() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1}, new double[] {1}, new double[] {2}, false);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported16() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1}, new double[] {1}, new double[] {2}, "M", false);
  }

  @Test
  public void testGetAdditiveShiftedSurface3() {
    final double[] x = new double[] {1};
    final double[] y = new double[] {1};
    final double[] shift = new double[] {2};
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, true);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName, true);
    expected = new InterpolatedSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, newName, true);
    expected = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName, true);
    expected = new NodalSurfaceAdditiveShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }

  @Test
  public void testGetMultiplicativeShiftedSurface3() {
    final double[] x = new double[] {1};
    final double[] y = new double[] {1};
    final double[] shift = new double[] {2};
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, false);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName, false);
    expected = new InterpolatedSurfaceMultiplicativeShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName, false);
    expected = new NodalSurfaceMultiplicativeShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }
}
