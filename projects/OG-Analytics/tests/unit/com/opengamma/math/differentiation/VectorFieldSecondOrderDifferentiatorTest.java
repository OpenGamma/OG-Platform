/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class VectorFieldSecondOrderDifferentiatorTest {

  private static Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
      double a = x.getEntry(0);
      double theta = x.getEntry(1);
      double[] temp = new double[2];
      double c1 = Math.cos(theta);
      temp[0] = a * c1 * c1;
      temp[1] = a * (1 - c1 * c1);
      return new DoubleMatrix1D(temp);
    }
  };

  private static Function1D<DoubleMatrix1D, DoubleMatrix2D> DW1 = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
      double a = x.getEntry(0);
      double theta = x.getEntry(1);
      double[][] temp = new double[2][2];
      double c1 = Math.cos(theta);
      double s1 = Math.sin(theta);
      temp[0][0] = 0.0;
      temp[1][1] = 2 * a * (1 - 2 * c1 * c1);
      temp[0][1] = -2 * s1 * c1;
      temp[1][0] = temp[0][1];
      return new DoubleMatrix2D(temp);
    }
  };

  private static Function1D<DoubleMatrix1D, DoubleMatrix2D> DW2 = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
      double a = x.getEntry(0);
      double theta = x.getEntry(1);
      double[][] temp = new double[2][2];
      double c1 = Math.cos(theta);
      double s1 = Math.sin(theta);
      temp[0][0] = 0.0;
      temp[1][1] = 2 * a * (2 * c1 * c1 - 1);
      temp[0][1] = 2 * s1 * c1;
      temp[1][0] = temp[0][1];
      return new DoubleMatrix2D(temp);
    }
  };

  @Test
  public void test() {
    double a = 2.3;
    double theta = 0.34;
    DoubleMatrix1D x = new DoubleMatrix1D(new double[] {a, theta });

    VectorFieldSecondOrderDifferentiator fd = new VectorFieldSecondOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix2D[]> fdFuncs = fd.differentiateFull(FUNC);
    DoubleMatrix2D[] fdValues = fdFuncs.evaluate(x);

    DoubleMatrix2D t1 = DW1.evaluate(x);
    DoubleMatrix2D t2 = DW2.evaluate(x);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals("first observation " + i + " " + j, t1.getEntry(i, j), fdValues[0].getEntry(i, j), 1e-6);
        assertEquals("second observation " + i + " " + j, t2.getEntry(i, j), fdValues[1].getEntry(i, j), 1e-6);
      }
    }

  }

}
