/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;


/**
 * 
 */
public class BoyleTrinomialOptionModelDefinition extends TrinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public double getDX(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, option.getStrike());
    final double dt = t / n;
    return sigma * Math.sqrt(2 * dt);
  }

  @Override
  public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, option.getStrike());
    final double b = data.getCostOfCarry();
    final double dt = t / n;
    final double nu = dt * (b - 0.5 * sigma * sigma);
    final double dx = getDX(option, data, n, j);
    return 0.5 * ((sigma * sigma * dt + nu * nu) / (dx * dx) - nu / dx);
  }

  @Override
  public double getMidFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    return 1 - getDownFactor(option, data, n, j) - getUpFactor(option, data, n, j);
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, option.getStrike());
    final double b = data.getCostOfCarry();
    final double dt = t / n;
    final double nu = dt * (b - 0.5 * sigma * sigma);
    final double dx = getDX(option, data, n, j);
    return getDownFactor(option, data, n, j) + nu / dx;
  }
}
