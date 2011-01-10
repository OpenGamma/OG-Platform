/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SABRDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan;
import com.opengamma.math.surface.ConstantDoublesSurface;

/**
 * 
 */
public class SABRBlackEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<OptionDefinition, SABRDataBundle> {
  private static final SABRFormulaHagan SABR = new SABRFormulaHagan();

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

    return new VolatilitySurface(ConstantDoublesSurface.from(SABR.impliedVolitility(f, alpha, beta, ksi, rho, k, t)));
  }

}
