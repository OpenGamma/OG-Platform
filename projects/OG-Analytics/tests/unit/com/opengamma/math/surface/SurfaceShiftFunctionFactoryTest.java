/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import static org.junit.Assert.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static com.opengamma.math.surface.SurfaceShiftFunctionFactory.getFunction;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
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
      return null;
    }

    @Override
    public Double[] getYData() {
      return null;
    }

    @Override
    public Double[] getZData() {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Double getZValue(final Double x, final Double y) {
      return null;
    }

    @Override
    public Double getZValue(final Pair<Double, Double> xy) {
      return null;
    }

  };
  private static final ConstantDoublesSurface CONSTANT = ConstantDoublesSurface.from(3.4);
  private static final FunctionalDoublesSurface FUNCTIONAL = FunctionalDoublesSurface.from(F);
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final InterpolatedDoublesSurface INTERPOLATED = InterpolatedDoublesSurface.from(new double[] {1, 2, 1, 2 }, new double[] {1, 2, 3, 4 }, new double[] {1.2, 3.4, 5.6, 7.8 },
      new GridInterpolator2D(LINEAR,
          LINEAR));
  @SuppressWarnings("unchecked")
  private static final InterpolatedFromCurvesDoublesSurface INTERPOLATED_FROM_CURVES = InterpolatedFromCurvesDoublesSurface.from(true, new double[] {1 },
      new Curve[] {NodalDoublesCurve.from(new double[] {1, 2 }, new double[] {3, 4 }) }, LINEAR);
  private static final NodalDoublesSurface NODAL = NodalDoublesSurface.from(new double[] {1, 2 }, new double[] {1, 2 }, new double[] {1.2, 3.4 });

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongClass() {
    SurfaceShiftFunctionFactory.getFunction(Double.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType1() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, .2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType2() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, 1, 2, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType3() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, new double[] {1 }, new double[] {2 }, new double[] {3 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType4() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, .2, "N");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType5() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, 1, 2, 3, "N");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveType6() {
    SurfaceShiftFunctionFactory.getShiftedSurface(DUMMY, new double[] {1 }, new double[] {2 }, new double[] {3 }, "N");
  }

  @Test
  public void testGetFunction() {
    assertEquals(ConstantSurfaceShiftFunction.class, getFunction(ConstantSurfaceShiftFunction.class).getClass());
    assertEquals(FunctionalSurfaceShiftFunction.class, getFunction(FunctionalSurfaceShiftFunction.class).getClass());
    assertEquals(InterpolatedSurfaceShiftFunction.class, getFunction(InterpolatedSurfaceShiftFunction.class).getClass());
    assertEquals(InterpolatedFromCurvesSurfaceShiftFunction.class, getFunction(InterpolatedFromCurvesSurfaceShiftFunction.class).getClass());
    assertEquals(NodalSurfaceShiftFunction.class, getFunction(NodalSurfaceShiftFunction.class).getClass());
  }

  @Test
  public void testGetShiftedSurface1() {
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift);
    Surface<Double, Double, Double> expected = new ConstantSurfaceShiftFunction().evaluate(CONSTANT, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.), 0);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift);
    expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, shift);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift);
    expected = new FunctionalSurfaceShiftFunction().evaluate(FUNCTIONAL, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));

    final String newName = "F";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, shift, newName);
    expected = new ConstantSurfaceShiftFunction().evaluate(CONSTANT, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, shift, newName);
    expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, shift, newName);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, shift, newName);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, shift, newName);
    assertEquals(expected, shifted);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, shift, newName);
    expected = new FunctionalSurfaceShiftFunction().evaluate(FUNCTIONAL, shift, newName);
    assertEquals(shifted.getClass(), expected.getClass());
    assertEquals(shifted.getZValue(1., 2.), expected.getZValue(1., 2.));
    assertEquals(shifted.getName(), expected.getName());
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported1() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported2() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, 1, 1, 2, "M");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported3() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported4() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, 1, 1, 2, "M");
  }

  @Test
  public void testGetShiftedSurface2() {
    final double x = 1;
    final double y = 1;
    final double shift = 2;
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName);
    expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported5() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1 }, new double[] {1 }, new double[] {2 });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported6() {
    SurfaceShiftFunctionFactory.getShiftedSurface(CONSTANT, new double[] {1 }, new double[] {1 }, new double[] {2 }, "M");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported7() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1 }, new double[] {1 }, new double[] {2 });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetShiftedSurfaceUnsupported8() {
    SurfaceShiftFunctionFactory.getShiftedSurface(FUNCTIONAL, new double[] {1 }, new double[] {1 }, new double[] {2 }, "M");
  }

  @Test
  public void testGetShiftedSurface3() {
    final double[] x = new double[] {1 };
    final double[] y = new double[] {1 };
    final double[] shift = new double[] {2 };
    Surface<Double, Double, Double> shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift);
    Surface<Double, Double, Double> expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    assertEquals(((InterpolatedDoublesSurface) shifted).getInterpolator(), ((InterpolatedDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getCurves(), ((InterpolatedFromCurvesDoublesSurface) expected).getCurves());
    assertEquals(((InterpolatedFromCurvesDoublesSurface) shifted).getInterpolator(), ((InterpolatedFromCurvesDoublesSurface) expected).getInterpolator());
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, x, y, shift);
    assertEquals(shifted.getClass(), expected.getClass());
    assertArrayEquals(shifted.getXData(), expected.getXData());
    assertArrayEquals(shifted.getYData(), expected.getYData());
    final String newName = "H";
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED, x, y, shift, newName);
    expected = new InterpolatedSurfaceShiftFunction().evaluate(INTERPOLATED, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    expected = new InterpolatedFromCurvesSurfaceShiftFunction().evaluate(INTERPOLATED_FROM_CURVES, x, y, shift, newName);
    assertEquals(shifted, expected);
    shifted = SurfaceShiftFunctionFactory.getShiftedSurface(NODAL, x, y, shift, newName);
    expected = new NodalSurfaceShiftFunction().evaluate(NODAL, x, y, shift, newName);
    assertEquals(shifted, expected);
  }
}
