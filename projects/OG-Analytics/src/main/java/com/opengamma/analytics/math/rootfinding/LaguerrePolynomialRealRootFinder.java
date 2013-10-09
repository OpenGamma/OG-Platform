/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.solvers.LaguerreSolver;
import org.apache.commons.math.complex.Complex;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * Class that calculates the real roots of a polynomial using Laguerre's method. This class is a wrapper for the
 * <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/solvers/LaguerreSolver.html">Commons Math library implementation</a>
 * of Laguerre's method.
 */
//TODO Have a complex and real root finder
public class LaguerrePolynomialRealRootFinder implements Polynomial1DRootFinder<Double> {
  private static final LaguerreSolver ROOT_FINDER = new LaguerreSolver();
  private static final Double[] EMPTY_ARRAY = new Double[0];
  private static final double EPS = 1e-16;

  /**
   * {@inheritDoc}
   * @throws MathException If there are no real roots; if the Commons method could not evaluate the function; if the Commons method could not converge.
   */
  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    ArgumentChecker.notNull(function, "function");
    try {
      final Complex[] roots = ROOT_FINDER.solveAll(function.getCoefficients(), 0);
      final List<Double> realRoots = new ArrayList<>();
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
