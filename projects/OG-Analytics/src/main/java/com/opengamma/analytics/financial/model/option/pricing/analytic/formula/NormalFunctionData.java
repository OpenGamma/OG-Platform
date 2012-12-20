/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

/**
 * A data bundle with the data require for the normal option model (Bachelier model).
 */
public class NormalFunctionData {

  /**
   * The forward.
   */
  private final double _forward;
  /**
   * The numeraire.
   */
  private final double _numeraire;
  /**
   * The normal volatility.
   */
  private final double _volatility;

  /**
   * Data bundle for pricing in a normal framework. That is, the forward value of the underlying asset is a martingale in the chosen numeraire measure. 
   * @param forward The forward value of the underlying asset (i.e. the forward value of a stock, the forward Libor rate, etc)
   * @param numeraire The numeraire associated to the equation.
   * @param sigma The normal volatility.
   */
  public NormalFunctionData(final double forward, final double numeraire, final double sigma) {
    _forward = forward;
    _numeraire = numeraire;
    _volatility = sigma;
  }

  public double getForward() {
    return _forward;
  }

  public double getNumeraire() {
    return _numeraire;
  }

  public double getNormalVolatility() {
    return _volatility;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NormalFunctionData[");
    sb.append("forward=");
    sb.append(_forward);
    sb.append(", numeraire=");
    sb.append(_numeraire);
    sb.append(", volatility=");
    sb.append(_volatility);
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
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volatility);
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
    final NormalFunctionData other = (NormalFunctionData) obj;
    if (Double.doubleToLongBits(_numeraire) != Double.doubleToLongBits(other._numeraire)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    return Double.doubleToLongBits(_volatility) == Double.doubleToLongBits(other._volatility);
  }

}
