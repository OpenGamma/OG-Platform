package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoublesCurveInterpolatedAnchorTest {

  private static final String CURVE_NAME = "Anchor";
  private static final double[] NODE = new double[] {0.1, 1.0, 3.0, 10.0};
  private static final double ANCHOR = 5.0;
  private static final double[] NODE_WITH_ANCHOR = new double[] {0.1, 1.0, 3.0, ANCHOR, 10.0};
  private static final double[] VALUE = new double[] {0.001, 0.002, -0.001, -0.005};
  private static final double[] VALUE_WITH_ANCHOR = new double[] {0.001, 0.002, -0.001, 0.0, -0.005};
  private static final Interpolator1D LINEAR = new LinearInterpolator1D();
  private static final DoublesCurveInterpolatedAnchor CURVE = DoublesCurveInterpolatedAnchor.from(NODE, VALUE, ANCHOR, LINEAR, CURVE_NAME);

  private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNode() {
    DoublesCurveInterpolatedAnchor.from(null, VALUE, ANCHOR, LINEAR, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullValue() {
    DoublesCurveInterpolatedAnchor.from(NODE, null, ANCHOR, LINEAR, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInterpolator() {
    DoublesCurveInterpolatedAnchor.from(NODE, VALUE, ANCHOR, null, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    DoublesCurveInterpolatedAnchor.from(NODE, VALUE, ANCHOR, LINEAR, null);
  }

  @Test
  public void getter() {
    assertEquals("DoublesCurveInterpolatedAnchor: getter", 3, CURVE.getAnchorIndex());
    assertArrayEquals("DoublesCurveInterpolatedAnchor: getter", NODE_WITH_ANCHOR, CURVE.getXDataAsPrimitive(), TOLERANCE);
    assertArrayEquals("DoublesCurveInterpolatedAnchor: getter", VALUE_WITH_ANCHOR, CURVE.getYDataAsPrimitive(), TOLERANCE);
    assertEquals("DoublesCurveInterpolatedAnchor: getter", NODE.length, CURVE.size());
  }

  @Test
  public void compareInterpolation() {
    InterpolatedDoublesCurve curveInterpolated = InterpolatedDoublesCurve.from(NODE_WITH_ANCHOR, VALUE_WITH_ANCHOR, LINEAR);
    int nbPts = 10;
    for (int looppt = 0; looppt <= nbPts; looppt++) {
      double x = NODE[0] + looppt * (NODE[NODE.length - 1] - NODE[0]) / nbPts;
      assertEquals("DoublesCurveInterpolatedAnchor: value", curveInterpolated.getYValue(x), CURVE.getYValue(x), TOLERANCE);
    }
  }

  @Test
  public void sensitivity() {
    InterpolatedDoublesCurve curveInterpolated = InterpolatedDoublesCurve.from(NODE_WITH_ANCHOR, VALUE_WITH_ANCHOR, LINEAR);
    int nbPts = 10;
    for (int looppt = 0; looppt <= nbPts; looppt++) {
      double x = NODE[0] + looppt * (NODE[NODE.length - 1] - NODE[0]) / nbPts;
      Double[] sensiAnchor = CURVE.getYValueParameterSensitivity(x);
      Double[] sensiInterpolated = curveInterpolated.getYValueParameterSensitivity(x);
      for (int loopnode = 0; loopnode < 3; loopnode++) { // Before Anchor
        assertEquals("DoublesCurveInterpolatedAnchor: value", sensiInterpolated[loopnode], sensiAnchor[loopnode], TOLERANCE);
      }
      assertEquals("DoublesCurveInterpolatedAnchor: value", sensiInterpolated[4], sensiAnchor[3], TOLERANCE);
    }
  }

}
