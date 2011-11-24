/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;

/**
 * A calculator to determine the upper limit of the Fourier integral for a characteristic function {@latex.inline $\\phi$}.
 * <p>
 * The upper limit is found by determining the root of the function:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * f(x) = \\ln\\left(\\left|\\phi(x - i(1 + \\alpha))\\right|\\right)
 * \\end{align*}
 * }
 * where {@latex.inline $\\alpha$} is the contour (which is parallel to the real axis and shifted down by {@latex.inline $1 + \\alpha$}) over which to
 * integrate.
 * 
 */
public class IntegralLimitCalculator {
  private static Logger s_log = LoggerFactory.getLogger(IntegralLimitCalculator.class);
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new BrentSingleRootFinder(1e-1);

  /**
   * 
   * @param psi The characteristic function, not null
   * @param alpha The value of {@latex.inline $\\alpha$}, not 0 or -1
   * @param tol The tolerance for the root
   * @return The root
   */
  public double solve(final Function1D<ComplexNumber, ComplexNumber> psi, final double alpha, final double tol) {
    Validate.notNull(psi, "psi null");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(tol > 0.0, "need tol > 0");

    final double k = Math.log(tol) + Math.log(ComplexMathUtils.mod(psi.evaluate(new ComplexNumber(0.0, -(1 + alpha)))));
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final ComplexNumber z = new ComplexNumber(x, -(1 + alpha));
        return Math.log(ComplexMathUtils.mod(psi.evaluate(z))) - k;
      }
    };
    double[] range = null;
    try {
      range = s_bracketRoot.getBracketedPoints(f, 0.0, 200.0);
    } catch (MathException e) {
      s_log.warn("Could not find integral limit. Using default of 500");
      return 500.0;
    }
    return s_root.getRoot(f, range[0], range[1]);
  }

}
