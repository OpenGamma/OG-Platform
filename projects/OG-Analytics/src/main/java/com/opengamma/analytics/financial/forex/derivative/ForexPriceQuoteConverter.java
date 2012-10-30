/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForexPriceQuoteConverter {

  private final double _domPips;
  private final double _s0;
  private final double _k;
  private final double _nd;
  private final double _nf;

  private final double _pcForeign;
  private final double _pcDom;
  private final double _foreignPips;
  private final double _absDom;
  private final double _absForeign;

  public ForexPriceQuoteConverter(final double domesticPipsPrice, final double spotRate, final double strike, final double domesticNotional, final double foreignNotional) {

    ArgumentChecker.isTrue(domesticPipsPrice >= 0.0, "Negative price given");
    ArgumentChecker.isTrue(spotRate > 0.0, "Spot rate must be greater than zero. value gvien is {}", spotRate);
    ArgumentChecker.isTrue(strike > 0.0, "Strike  must be greater than zero. value gvien is {}", strike);
    ArgumentChecker.isTrue(domesticNotional > 0.0, "Domestic Notional must be greater than zero. value gvien is {}", domesticNotional);
    ArgumentChecker.isTrue(foreignNotional > 0.0, "Foreign Notional must be greater than zero. value gvien is {}", foreignNotional);
    _domPips = domesticPipsPrice;
    _s0 = spotRate;
    _k = strike;
    _nd = domesticNotional;
    _nf = foreignNotional;

    _pcForeign = _domPips / _s0;
    _pcDom = _domPips / _k;
    _foreignPips = _pcForeign / _k;
    _absDom = _nd * _domPips;
    _absForeign = _nf * _pcForeign;
  }

  /**
   * Gets the domestic Pips (points) or domestic/foreign price.
   * @return the domPips
   */
  public double getDomesticPips() {
    return _domPips;
  }

  /**
   * Gets the percentage foreign price.
   * @return the pcForeign
   */
  public double getPcForeign() {
    return _pcForeign;
  }

  /**
   * Gets the percentage domestic price.
   * @return the pcDom
   */
  public double getPcDom() {
    return _pcDom;
  }

  /**
   * Gets the foreign Pips (points) or foreign/domestic price.
   * @return the foreignPips
   */
  public double getForeignPips() {
    return _foreignPips;
  }

  /**
   * Gets the absolute domestic price.
   * @return the absDom
   */
  public double getAbsDom() {
    return _absDom;
  }

  /**
   * Gets the absolute Foreign price.
   * @return the absForeign
   */
  public double getAbsForeign() {
    return _absForeign;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_domPips);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_k);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_nd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_nf);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_s0);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForexPriceQuoteConverter other = (ForexPriceQuoteConverter) obj;
    if (Double.doubleToLongBits(_domPips) != Double.doubleToLongBits(other._domPips)) {
      return false;
    }
    if (Double.doubleToLongBits(_k) != Double.doubleToLongBits(other._k)) {
      return false;
    }
    if (Double.doubleToLongBits(_nd) != Double.doubleToLongBits(other._nd)) {
      return false;
    }
    if (Double.doubleToLongBits(_nf) != Double.doubleToLongBits(other._nf)) {
      return false;
    }
    if (Double.doubleToLongBits(_s0) != Double.doubleToLongBits(other._s0)) {
      return false;
    }
    return true;
  }



}
