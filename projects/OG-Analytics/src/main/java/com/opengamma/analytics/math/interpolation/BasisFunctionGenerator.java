/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class BasisFunctionGenerator {

  /**
   * Generates a set of b-splines with knots a fixed distance apart 
   * @param xa minimum value of the function domain 
   * @param xb maximum value of the function domain
   * @param nKnots number of internal knots (minimum 2 - 1 at xa and 1 at xb)
   * @param degree The order of the polynomial splines 
   * @return a List of functions 
   */
  public List<Function1D<Double, Double>> generateSet(double xa, double xb, int nKnots, final int degree) {

    int n = nKnots + 2 * degree;
    int nSplines = nKnots + degree - 1;
    List<Function1D<Double, Double>> functions = new ArrayList<Function1D<Double, Double>>(nSplines);
    double[] knots = new double[n];
    double dx = (xb - xa) / (nKnots - 1);

    // knots to the left and right of the range
    for (int i = 0; i < degree; i++) {
      knots[i] = (i - degree) * dx + xa;
      knots[degree + nKnots + i] = xb + dx * (i + 1);
    }
    // knots in the main range
    for (int i = 0; i < nKnots - 1; i++) {
      knots[i + degree] = xa + i * dx;
    }
    knots[nKnots + degree - 1] = xb;

    for (int i = 0; i < nSplines; i++) {
      functions.add(generate(knots, degree, i));
    }
    return functions;
  }

  public List<Function1D<double[], Double>> generateSet(double[] xa, double[] xb, int[] nKnots, final int[] degree) {

    int dim = xa.length;
    Validate.isTrue(dim == xb.length, "xb wrong dimension");
    Validate.isTrue(dim == nKnots.length, "nKnots wrong dimension");
    Validate.isTrue(dim == degree.length, "degree wrong dimension");

    int[] n = new int[dim];
    int[] nSplines = new int[dim];
    double[][] knots = new double[dim][];

    double[] dx = new double[dim];
    int product = 1;
    for (int k = 0; k < dim; k++) {
      int p = nKnots[k] + 2 * degree[k];
      n[k] = p;
      knots[k] = new double[p];
      nSplines[k] = nKnots[k] + degree[k] - 1;
      product *= nSplines[k];
      dx[k] = (xb[k] - xa[k]) / (nKnots[k] - 1);

      // knots to the left and right of the range
      for (int i = 0; i < degree[k]; i++) {
        knots[k][i] = (i - degree[k]) * dx[k] + xa[k];
        knots[k][degree[k] + nKnots[k] + i] = xb[k] + dx[k] * (i + 1);
      }
      // knots in the main range
      for (int i = 0; i < nKnots[k] - 1; i++) {
        knots[k][i + degree[k]] = xa[k] + i * dx[k];
      }
      knots[k][nKnots[k] + degree[k] - 1] = xb[k];
    }

    List<Function1D<double[], Double>> functions = new ArrayList<Function1D<double[], Double>>(product);

    for (int i = 0; i < product; i++) {
      int[] indicies = FunctionUtils.fromTensorIndex(i, nSplines);
      functions.add(generate(knots, degree, indicies));
    }
    return functions;
  }

  public List<Function1D<Double, Double>> generateSet(double[] internalKnots, final int degree) {

    int nKnots = internalKnots.length;
    int n = nKnots + 2 * degree;
    double[] knots = new double[n];

    double dxa = internalKnots[1] - internalKnots[0];
    double dxb = internalKnots[nKnots - 1] - internalKnots[nKnots - 2];

    List<Function1D<Double, Double>> functions = new ArrayList<Function1D<Double, Double>>(n);

    // knots to the left and right of the range
    for (int i = 0; i < degree; i++) {
      knots[i] = (i - degree) * dxa + internalKnots[0];
      knots[degree + nKnots + i] = internalKnots[nKnots - 1] + dxb * (i + 1);
    }
    // knots in the main range
    for (int i = 0; i < nKnots; i++) {
      knots[i + degree] = internalKnots[i];
    }

    int nSplines = nKnots + degree - 1;
    for (int i = 0; i < nSplines; i++) {
      functions.add(generate(knots, degree, i));
    }
    return functions;
  }

  public Function1D<Double, Double> generate(double xa, double xb, int nKnots, final int degree, final int j) {
    Validate.isTrue(xb > xa, "the range should be from xa to xb");
    Validate.isTrue(j >= 0, "basis functions are index from zero");
    Validate.isTrue(degree >= 0, "degree must zero or more");

    double[] knots = new double[nKnots + 2 * degree];
    double dx = (xb - xa) / (nKnots - 1);

    // knots to the left and right of the range
    for (int i = 0; i < degree; i++) {
      knots[i] = (i - degree) * dx + xa;
      knots[nKnots + i] = xb + (i + 1);
    }
    // knots in the main range
    for (int i = 0; i < nKnots - 1; i++) {
      knots[i + degree] = xa + i * dx;
    }
    knots[nKnots - 1] = xb;

    return generate(knots, degree, j);
  }

  public Function1D<double[], Double> generate(final double[][] knots, final int[] degree, final int[] index) {
    Validate.notNull(knots, "knots are null");
    final int dim = knots.length;
    Validate.isTrue(dim == degree.length, "degree wrong dimension");
    Validate.isTrue(dim == index.length, "index wrong dimension");
    final List<Function1D<Double, Double>> funcs = new ArrayList<Function1D<Double, Double>>(dim);
    for (int i = 0; i < dim; i++) {
      funcs.add(generate(knots[i], degree[i], index[i]));
    }
    return new Function1D<double[], Double>() {

      @Override
      public Double evaluate(double[] x) {
        double product = 1.0;
        Validate.isTrue(dim == x.length);
        for (int i = 0; i < dim; i++) {
          product *= funcs.get(i).evaluate(x[i]);
        }
        return product;
      }
    };

  }

  public Function1D<Double, Double> generate(final double[] knots, final int degree, final int j) {

    Validate.notNull(knots, "knots are null");
    Validate.isTrue(j >= 0, "basis functions are index from zero");
    Validate.isTrue(degree >= 0, "degree must zero or more");
    int m = knots.length;
    Validate.isTrue(j < m - degree - 1, "last basis function index is degree + 1 less than last knot index");

    if (degree == 0) {
      return new Function1D<Double, Double>() {

        @Override
        public Double evaluate(Double x) {
          return (x >= knots[j] && x < knots[j + 1]) ? 1.0 : 0.0;
        }
      };
    }

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        Function1D<Double, Double> fa = generate(knots, degree - 1, j);
        Function1D<Double, Double> fb = generate(knots, degree - 1, j + 1);
        return (x - knots[j]) / (knots[j + degree] - knots[j]) * fa.evaluate(x) + (knots[j + degree + 1] - x) / (knots[j + degree + 1] - knots[j + 1]) * fb.evaluate(x);
      }

    };

  }
}
