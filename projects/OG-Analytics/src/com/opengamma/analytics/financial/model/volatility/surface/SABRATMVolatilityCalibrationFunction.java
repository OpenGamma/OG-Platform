/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SABRDataBundle;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.rootfinding.CubicRealRootFinder;
import com.opengamma.analytics.math.rootfinding.Polynomial1DRootFinder;
import com.opengamma.analytics.math.rootfinding.QuadraticRealRootFinder;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRATMVolatilityCalibrationFunction {
  private static final Polynomial1DRootFinder<Double> QUADRATIC_FINDER = new QuadraticRealRootFinder();
  private static final Polynomial1DRootFinder<Double> ROOT_FINDER = new CubicRealRootFinder();

  public SABRDataBundle calibrate(final OptionDefinition option, final SABRDataBundle data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    final double beta = data.getBeta();
    final double t = option.getTimeToExpiry(data.getDate());
    final double rho = data.getRho();
    final double ksi = data.getVolOfVol();
    final double sigmaATM = data.getVolatility(t, option.getStrike());
    final double b = data.getCostOfCarry();
    final double f = data.getSpot() * Math.exp(b * t);
    final double beta1 = 1 - beta;
    final double f1 = Math.pow(f, beta1);
    final double a0 = -sigmaATM * f1;
    final double a1 = 1 + (2 - 3 * rho * rho) * ksi * ksi * t / 24;
    final double a2 = rho * beta * ksi * t / 4 / f1;
    final double a3 = beta1 * beta1 * t / 24 / f1 / f1;
    Double[] roots;
    if (CompareUtils.closeEquals(a3, 0, 1e-16)) {
      roots = QUADRATIC_FINDER.getRoots(new RealPolynomialFunction1D(new double[] {a0, a1, a2}));
    } else {
      roots = ROOT_FINDER.getRoots(new RealPolynomialFunction1D(new double[] {a0, a1, a2, a3}));
    }
    Arrays.sort(roots);
    if (roots[0] > 0) {
      return data.withAlpha(roots[0]);
    }
    for (final Double r : roots) {
      if (r > 0) {
        return data.withAlpha(r);
      }
    }
    throw new MathException("Could not find positive real root");
  }

}
