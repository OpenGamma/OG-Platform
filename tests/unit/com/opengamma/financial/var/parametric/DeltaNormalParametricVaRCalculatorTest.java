package com.opengamma.financial.var.parametric;

import javax.time.period.Period;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import com.opengamma.util.time.Tenor;

public class DeltaNormalParametricVaRCalculatorTest {

  @Test
  public void test() {
    final double[][] correlation = { new double[] { 1, 0.91, 0.914, 0.912, 0.888 }, new double[] { 0.91, 1, 0.9492, 0.9276, 0.9173 },
        new double[] { 0.914, 0.9492, 1, 0.96, 0.95 }, new double[] { 0.912, 0.9276, 0.96, 1, 0.9639 }, new double[] { 0.888, 0.9173, 0.95, 0.9639, 1 } };
    final double[] sigma = new double[] { 0.587, 0.5509, 0.4982, 0.4587, 0.4185 };
    final double[] v = new double[] { -5750250, -2297200, -769000, 1155000, 1960875 };
    final DoubleMatrix2D covariance = DoubleFactory2D.dense.make(5, 5);
    final DoubleMatrix1D valueDelta = DoubleFactory1D.dense.make(5);
    double c;
    for (int i = 0; i < 5; i++) {
      covariance.setQuick(i, i, sigma[i] * sigma[i]);
      valueDelta.setQuick(i, v[i]);
      for (int j = 0; j < i; j++) {
        c = correlation[i][j] * sigma[i] * sigma[j];
        covariance.setQuick(i, j, c);
        covariance.setQuick(j, i, c);
      }
    }
    final DeltaNormalParametricVaRCalculator calculator = new DeltaNormalParametricVaRCalculator(Tenor.DAY);
    System.out.println(calculator.getStaticVaR(valueDelta, covariance, new Tenor(Period.days(10)), 0.99));
  }
}
