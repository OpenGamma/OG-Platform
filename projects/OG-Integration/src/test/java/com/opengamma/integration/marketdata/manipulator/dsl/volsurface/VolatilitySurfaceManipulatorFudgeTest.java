/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceManipulatorFudgeTest extends AbstractFudgeBuilderTestCase {

  public void parallelShift() {
    VolatilitySurfaceParallelShift shift = new VolatilitySurfaceParallelShift(0.123);
    assertEncodeDecodeCycle(VolatilitySurfaceParallelShift.class, shift);
  }

  public void multipleAdditiveShifts() {
    VolatilitySurfaceMultipleAdditiveShifts shifts = new VolatilitySurfaceMultipleAdditiveShifts(
        new double[] {1, 2, 3 },
        new double[] {4, 5, 6 },
        new double[] {7, 8, 9 });
    assertEncodeDecodeCycle(VolatilitySurfaceMultipleAdditiveShifts.class, shifts);
  }

  public void constantMultiplicativeShift() {
    VolatilitySurfaceConstantMultiplicativeShift shift = new VolatilitySurfaceConstantMultiplicativeShift(0.321);
    assertEncodeDecodeCycle(VolatilitySurfaceConstantMultiplicativeShift.class, shift);
  }

  public void multipleMultiplicativeShifts() {
    VolatilitySurfaceMultipleMultiplicativeShifts shifts = new VolatilitySurfaceMultipleMultiplicativeShifts(
        new double[] {1, 2, 3 },
        new double[] {4, 5, 6 },
        new double[] {7, 8, 9 });
    assertEncodeDecodeCycle(VolatilitySurfaceMultipleMultiplicativeShifts.class, shifts);
  }

  public void singleAdditiveShift() {
    VolatilitySurfaceSingleAdditiveShift shift = new VolatilitySurfaceSingleAdditiveShift(0.1, 0.2, 0.3);
    assertEncodeDecodeCycle(VolatilitySurfaceSingleAdditiveShift.class, shift);
  }

  public void singleMultiplicativeShift() {
    VolatilitySurfaceSingleMultiplicativeShift shift = new VolatilitySurfaceSingleMultiplicativeShift(1.1, 1.2, 1.3);
    assertEncodeDecodeCycle(VolatilitySurfaceSingleMultiplicativeShift.class, shift);
  }

}
