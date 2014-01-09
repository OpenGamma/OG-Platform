/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionalDoublesCubeTest {
  private static final String NAME1 = "P";
  private static final String NAME2 = "O";
  private static final Function<Double, Double> F1 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xyz) {
      return xyz[0] + xyz[1] + xyz[2];
    }

  };
  private static final Function<Double, Double> F2 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xyz) {
      return xyz[0] + xyz[1] * xyz[2];
    }

  };
  private static final FunctionalDoublesCube CUBE = new FunctionalDoublesCube(F1, NAME1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction1() {
    new FunctionalDoublesCube(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction2() {
    new FunctionalDoublesCube(null, NAME1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    CUBE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYData() {
    CUBE.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetZData() {
    CUBE.getZData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetData() {
    CUBE.getValues();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSize() {
    CUBE.size();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX() {
    CUBE.getValue(null, 2., 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY() {
    CUBE.getValue(1., null, 4.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullZ() {
    CUBE.getValue(1., 1., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTriple() {
    CUBE.getValue(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    FunctionalDoublesCube other = new FunctionalDoublesCube(F1, NAME1);
    assertEquals(CUBE, other);
    assertEquals(CUBE.hashCode(), other.hashCode());
    other = new FunctionalDoublesCube(F2, NAME1);
    assertFalse(CUBE.equals(other));
    other = new FunctionalDoublesCube(F1, NAME2);
    assertFalse(CUBE.equals(other));
    other = new FunctionalDoublesCube(F1);
    assertFalse(CUBE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(CUBE.getName(), NAME1);
    assertEquals(CUBE.getFunction(), F1);
    assertEquals(CUBE.getValue(1., 2., 3.), F1.evaluate(1., 2., 3.), 0);
    assertEquals(CUBE.getValue(new Triple<>(1., 2., 3.)), F1.evaluate(1., 2., 3.), 0);
  }

  @Test
  public void testStaticConstruction() {
    FunctionalDoublesCube cube = new FunctionalDoublesCube(F1);
    FunctionalDoublesCube other = FunctionalDoublesCube.from(F1);
    assertEquals(cube.getFunction(), other.getFunction());
    assertFalse(cube.equals(other));
    cube = new FunctionalDoublesCube(F1, NAME1);
    other = FunctionalDoublesCube.from(F1, NAME1);
    assertEquals(cube, other);
  }

}
