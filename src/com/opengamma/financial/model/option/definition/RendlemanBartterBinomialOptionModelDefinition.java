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
public class RendlemanBartterBinomialOptionModelDefinition extends BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double dt = t / n;
    return Math.exp((b - sigma * sigma / 2) * dt - sigma * Math.sqrt(dt));
  }

  @Override
  public double getProbability(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    return 0.5;
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
    final double b = data.getCostOfCarry();
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double dt = t / n;
    return Math.exp((b - sigma * sigma / 2) * dt + sigma * Math.sqrt(dt));
  }

}
