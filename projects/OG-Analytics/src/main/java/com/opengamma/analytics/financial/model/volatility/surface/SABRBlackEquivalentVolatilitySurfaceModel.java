/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SABRDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;

/**
 * 
 */
public class SABRBlackEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<OptionDefinition, SABRDataBundle> {
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

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
    return new VolatilitySurface(ConstantDoublesSurface.from(SABR_FUNCTION.getVolatilityFunction(new EuropeanVanillaOption(k, t, true), f).evaluate(new SABRFormulaData(alpha, beta, rho, ksi))));
  }
}
