/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRCubeUtilTest {

  private static final double TOLERANCE = 1.0E-10;

  @Test
  /**
   * Tests the toArray method.
   */
  public void toDoubleLabelledMatrix2DFull() {
    double[] x = new double[] {0.5, 2.5, 5.0};
    double[] y = new double[] {1.0, 2.0, 5.0, 10.0};
    double[][] vIn = new double[y.length][x.length];
    SurfaceValue surf = new SurfaceValue();
    for (int loopx = 0; loopx < x.length; loopx++) {
      for (int loopy = 0; loopy < y.length; loopy++) {
        vIn[loopy][loopx] = x[loopx] * y[loopy];
        surf.add(DoublesPair.of(x[loopx], y[loopy]), vIn[loopy][loopx]);
      }
    }
    DoubleLabelledMatrix2D vOut = SABRCubeUtils.toDoubleLabelledMatrix2D(surf);
    for (int loopx = 0; loopx < x.length; loopx++) {
      for (int loopy = 0; loopy < y.length; loopy++) {
        assertEquals("Surface value - toArray", vIn[loopy][loopx], vOut.getValues()[loopy][loopx], TOLERANCE);
      }
    }
    for (int loopx = 0; loopx < x.length; loopx++) {
      assertEquals("Surface value - toArray", x[loopx], vOut.getXKeys()[loopx], TOLERANCE);
    }
    for (int loopy = 0; loopy < y.length; loopy++) {
      assertEquals("Surface value - toArray", y[loopy], vOut.getYKeys()[loopy], TOLERANCE);
    }
  }

  @Test
  /**
   * Tests the toArray method.
   */
  public void toDoubleLabelledMatrix2DPartial() {
    double[] x = new double[] {0.5, 2.5, 5.0};
    double[] y = new double[] {1.0, 2.0, 5.0};
    double[][] vIn = new double[x.length][x.length];
    SurfaceValue surf = new SurfaceValue();
    for (int loopx = 0; loopx < x.length; loopx++) {
      vIn[loopx][loopx] = x[loopx] * y[loopx];
      surf.add(DoublesPair.of(x[loopx], y[loopx]), vIn[loopx][loopx]);
    }
    DoubleLabelledMatrix2D vOut = SABRCubeUtils.toDoubleLabelledMatrix2D(surf);
    for (int loopx = 0; loopx < x.length; loopx++) {
      assertEquals("Surface value - toArray", vIn[loopx][loopx], vOut.getValues()[loopx][loopx], TOLERANCE);
    }
    for (int loopx = 0; loopx < x.length; loopx++) {
      assertEquals("Surface value - toArray", x[loopx], vOut.getXKeys()[loopx], TOLERANCE);
    }
    for (int loopy = 0; loopy < x.length; loopy++) {
      assertEquals("Surface value - toArray", y[loopy], vOut.getYKeys()[loopy], TOLERANCE);
    }
  }

}
