/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.ConcatenatedVectorFunction;
import com.opengamma.analytics.financial.interestrate.capletstripping.VectorFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class VectorFunctionTest {

  @Test
  public void test() {

    final VectorFunction vf1 = new VectorFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        return new DoubleMatrix1D(1, x.getEntry(0) + 2 * x.getEntry(1));
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final DoubleMatrix2D jac = new DoubleMatrix2D(1, 2);
        jac.getData()[0][0] = 1.0;
        jac.getData()[0][1] = 2.0;
        return jac;
      }

      @Override
      public int getSizeOfDomain() {
        return 2;
      }

      @Override
      public int getSizeOfRange() {
        return 1;
      }
    };

    final VectorFunction vf2 = new VectorFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double x1 = x.getEntry(0);
        final double x2 = x.getEntry(1);
        final double y1 = x1 * x2;
        final double y2 = x2 * x2;
        return new DoubleMatrix1D(y1, y2);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final double x1 = x.getEntry(0);
        final double x2 = x.getEntry(1);
        final double j11 = x2;
        final double j12 = x1;
        final double j21 = 0.0;
        final double j22 = 2 * x2;
        return new DoubleMatrix2D(new double[][] { {j11, j12 }, {j21, j22 } });
      }

      @Override
      public int getSizeOfDomain() {
        return 2;
      }

      @Override
      public int getSizeOfRange() {
        return 2;
      }
    };

    final VectorFunction vf3 = new VectorFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double x1 = x.getEntry(0);
        final double y1 = x1;
        final double y2 = Math.sin(x1);
        return new DoubleMatrix1D(y1, y2);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final double x1 = x.getEntry(0);
        final double j11 = 1.0;
        final double j21 = Math.cos(x1);
        return new DoubleMatrix2D(new double[][] { {j11 }, {j21 } });
      }

      @Override
      public int getSizeOfDomain() {
        return 1;
      }

      @Override
      public int getSizeOfRange() {
        return 2;
      }
    };

    final DoubleMatrix1D x1 = new DoubleMatrix1D(new double[] {-2, 2 });
    final DoubleMatrix2D jac1 = vf1.evaluateJacobian(x1);
    System.out.println(jac1);

    final DoubleMatrix1D x2 = new DoubleMatrix1D(new double[] {1, 2 });
    final DoubleMatrix2D jac2 = vf2.evaluateJacobian(x2);
    System.out.println(jac2);

    final DoubleMatrix1D x3 = new DoubleMatrix1D(new double[] {Math.PI });
    final DoubleMatrix2D jac3 = vf3.evaluateJacobian(x3);
    System.out.println(jac3);

    final DoubleMatrix1D x = new DoubleMatrix1D(new double[] {-2, 2, 1, 2, Math.PI });
    final ConcatenatedVectorFunction vf = new ConcatenatedVectorFunction(new VectorFunction[] {vf1, vf2, vf3 });
    final DoubleMatrix2D jac = vf.evaluateJacobian(x);
    System.out.println(jac);
  }

}
