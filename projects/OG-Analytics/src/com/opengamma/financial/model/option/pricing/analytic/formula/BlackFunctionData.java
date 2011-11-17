/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;

/**
 * 
 */
public class BlackFunctionData {
  private final double _f;
  private final double _numeraire;
  private final double _sigma;

  /**
   * Data bundle for pricing in a Black framework. That is, the forward value of the underlying asset is a martingale in the chosen numeraire measure. 
   * @param forward The forward value of the underlying asset (i.e. the forward value of a stock, the forward Libor rate, etc)
   * @param numeraire The present value of the instrument used to discount the payoff (e.g. the zero coupon bond in the T-forward measure, the swap annuity for pricing 
   * swaptions, etc)
   * @param sigma The Black volatility 
   */
  public BlackFunctionData(final double forward, final double numeraire, final double sigma) {
    _f = forward;
    _numeraire = numeraire;
    _sigma = sigma;
  }

  public double getForward() {
    return _f;
  }

  public double getDiscountFactor() {
    return _numeraire;
  }

  public double getBlackVolatility() {
    return _sigma;
  }

  public static BlackFunctionData fromDataBundle(final BlackOptionDataBundle bundle, final EuropeanVanillaOptionDefinition definition) {
    Validate.notNull(bundle, "bundle");
    Validate.notNull(definition, "definition");
    final double t = definition.getTimeToExpiry(bundle.getDate());
    final double k = definition.getStrike();
    return new BlackFunctionData(bundle.getForward(), bundle.getDiscountFactor(t), bundle.getVolatility(t, k));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BlackFunctionData[");
    sb.append("f=");
    sb.append(getForward());
    sb.append(", numeraire=");
    sb.append(getDiscountFactor());
    sb.append(", sigma=");
    sb.append(getBlackVolatility());
    sb.append("]");
    return sb.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_numeraire);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_f);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigma);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BlackFunctionData other = (BlackFunctionData) obj;
    if (Double.doubleToLongBits(_numeraire) != Double.doubleToLongBits(other._numeraire)) {
      return false;
    }
    if (Double.doubleToLongBits(_f) != Double.doubleToLongBits(other._f)) {
      return false;
    }
    return Double.doubleToLongBits(_sigma) == Double.doubleToLongBits(other._sigma);
  }

}
