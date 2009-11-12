/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 * @author emcleod
 */
public class CoxRossRubinsteinBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double dt = t / n;
    return Math.exp(-sigma * Math.sqrt(dt));
  }

  @Override
  public double getProbability(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double dt = t / n;
    final double u = getUpFactor(option, data, n);
    final double d = getDownFactor(option, data, n);
    return (Math.exp(b * dt) - d) / (u - d);
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    return 1. / getDownFactor(option, data, n);
  }
}
