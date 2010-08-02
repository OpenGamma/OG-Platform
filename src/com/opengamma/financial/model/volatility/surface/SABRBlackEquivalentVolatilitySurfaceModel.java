/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SABRDataBundle;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRBlackEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<OptionDefinition, SABRDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRBlackEquivalentVolatilitySurfaceModel.class);
  private static final double EPS = 1e-15;

  @Override
  public VolatilitySurface getSurface(final Map<OptionDefinition, Double> optionData, final SABRDataBundle data) {
    Validate.notNull(optionData, "option data");
    Validate.notEmpty(optionData, "option data");
    Validate.notNull(data);
    if (optionData.size() > 1) {
      s_logger.warn("Have more than one option: only using the first");
    }
    final OptionDefinition option = optionData.keySet().iterator().next();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry(data.getDate());
    final double alpha = data.getAlpha();
    final double beta = data.getBeta();
    final double rho = data.getRho();
    final double ksi = data.getVolOfVol();
    final double f = data.getSpot();
    final double ln = Math.log(f / k);
    double sigmaEquivalent, z, chi;
    if (CompareUtils.closeEquals(beta, 0, EPS)) {
      z = ksi * Math.sqrt(f * k) * ln / alpha;
      chi = getChi(rho, z);
      sigmaEquivalent = alpha * z * (1 + t * (rho * alpha * ksi / 4 + ksi * ksi * (2 - 3 * rho * rho) / 24)) / chi;
    } else if (CompareUtils.closeEquals(beta, 1, EPS)) {
      z = ksi * ln / alpha;
      chi = getChi(rho, z);
      sigmaEquivalent = alpha * ln * z * (1 + t * (alpha * alpha / f / k + ksi * ksi * (2 - 3 * rho * rho)) / 24) / (f - k) / chi;
    } else {
      z = ksi * Math.pow(f * k, (1 - beta) / 2) * ln / alpha;
      chi = getChi(rho, z);
      final double beta1 = 1 - beta;
      if (CompareUtils.closeEquals(f, k, EPS)) {
        final double f1 = Math.pow(f, beta1);
        sigmaEquivalent = alpha * t * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * ksi / 4 / f1 + ksi * ksi * (2 - 3 * rho * rho) / 24) / f1;
      } else {
        final double first = alpha / (Math.pow(f * k, beta1 / 2) * (1 + Math.pow(beta1 * ln, 2) / 24 + Math.pow(beta1 * ln, 4) / 1920));
        final double second = z / chi;
        final double third = t * (1 + beta1 * beta1 * alpha * alpha / 24 / Math.pow(f * k, beta1) + rho * ksi * beta * alpha / 4 / Math.pow(f * k, beta1 / 2) + ksi * ksi * (2 - 3 * rho * rho) / 24);
        sigmaEquivalent = first * second * third;
      }
    }
    return new ConstantVolatilitySurface(sigmaEquivalent);
  }

  private double getChi(final double rho, final double z) {
    return Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z + rho) / (1 - rho));
  }
}
