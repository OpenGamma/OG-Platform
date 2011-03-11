/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public class IntegralLimitCalculator {
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder(1e-1);

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
    final double[] range = s_bracketRoot.getBracketedPoints(f, 0.0, 200.0);
    return s_root.getRoot(f, range[0], range[1]);
  }

}
