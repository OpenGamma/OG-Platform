/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.solvers.LaguerreSolver;
import org.apache.commons.math.complex.Complex;

import com.opengamma.math.MathException;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
//TODO Have a complex and real root finder
public class LaguerrePolynomialRealRootFinder implements Polynomial1DRootFinder<Double> {
  private static final LaguerreSolver ROOT_FINDER = new LaguerreSolver();
  private static final Double[] EMPTY_ARRAY = new Double[0];
  private static final double EPS = 1e-16;

  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    Validate.notNull(function, "function");
    try {
      final Complex[] roots = ROOT_FINDER.solveAll(function.getCoefficients(), 0);
      final List<Double> realRoots = new ArrayList<Double>();
      for (final Complex c : roots) {
        if (CompareUtils.closeEquals(c.getImaginary(), 0, EPS)) {
          realRoots.add(c.getReal());
        }
      }
      if (realRoots.isEmpty()) {
        throw new MathException("Could not find any real roots");
      }
      return realRoots.toArray(EMPTY_ARRAY);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }
}
