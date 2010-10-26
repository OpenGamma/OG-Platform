/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SABRDataBundle;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRBlackEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<OptionDefinition, SABRDataBundle> {
  private static final double EPS = 1e-15;

  @Override
  public VolatilitySurface getSurface(final OptionDefinition option, final SABRDataBundle data) {
    Validate.notNull(option, "option definition");
    Validate.notNull(data);
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry(data.getDate());
    final double alpha = data.getAlpha();
    final double beta = data.getBeta();
    final double rho = data.getRho();
    final double ksi = data.getVolOfVol();
    final double b = data.getCostOfCarry();
    final double f = data.getSpot() * Math.exp(b * t);
    double sigmaEquivalent, z, chi;
    final double beta1 = 1 - beta;
    if (CompareUtils.closeEquals(f, k, EPS)) {
      final double f1 = Math.pow(f, beta1);
      sigmaEquivalent = alpha * (1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * ksi / 4 / f1 + ksi * ksi * (2 - 3 * rho * rho) / 24)) / f1;
    } else {
      if (CompareUtils.closeEquals(beta, 0, EPS)) {
        final double ln = Math.log(f / k);
        z = ksi * Math.sqrt(f * k) * ln / alpha;
        chi = getChi(rho, z);
        sigmaEquivalent = alpha * ln * getRatio(chi, z) * (1 + t * (alpha * alpha / f / k + ksi * ksi * (2 - 3 * rho * rho)) / 24) / (f - k);
      } else if (CompareUtils.closeEquals(beta, 1, EPS)) {
        final double ln = Math.log(f / k);
        z = ksi * ln / alpha;
        chi = getChi(rho, z);
        sigmaEquivalent = alpha * getRatio(chi, z) * (1 + t * (rho * alpha * ksi / 4 + ksi * ksi * (2 - 3 * rho * rho) / 24));
      } else {
        final double ln = Math.log(f / k);
        final double f1 = Math.pow(f * k, beta1);
        final double f1Sqrt = Math.sqrt(f1);
        final double lnBetaSq = Math.pow(beta1 * ln, 2);
        z = ksi * f1Sqrt * ln / alpha;
        chi = getChi(rho, z);
        final double first = alpha / (f1Sqrt * (1 + lnBetaSq / 24 + lnBetaSq * lnBetaSq / 1920));
        final double second = getRatio(chi, z);
        final double third = 1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 + rho * ksi * beta * alpha / 4 / f1Sqrt + ksi * ksi * (2 - 3 * rho * rho) / 24);
        sigmaEquivalent = first * second * third;
      }
    }
    return new VolatilitySurface(ConstantDoublesSurface.from(sigmaEquivalent));
  }

  private double getChi(final double rho, final double z) {
    return Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho) / (1 - rho));
  }

  private double getRatio(final double chi, final double z) {
    if (CompareUtils.closeEquals(chi, 0, EPS) && CompareUtils.closeEquals(z, 0, EPS)) {
      return 1;
    }
    return z / chi;
  }
}
