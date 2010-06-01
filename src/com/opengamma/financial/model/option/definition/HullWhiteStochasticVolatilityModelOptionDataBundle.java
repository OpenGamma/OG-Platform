/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class HullWhiteStochasticVolatilityModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _sigmaLR;
  private final double _volOfVol;
  private final double _rho;

  public HullWhiteStochasticVolatilityModelOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date, final double lambda, final double sigmaLR, final double volOfVol, final double rho) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _sigmaLR = sigmaLR;
    _volOfVol = volOfVol;
    _rho = rho;
  }

  public HullWhiteStochasticVolatilityModelOptionDataBundle(final HullWhiteStochasticVolatilityModelOptionDataBundle data) {
    super(data);
    _lambda = data.getHalfLife();
    _sigmaLR = data.getLongRunVolatility();
    _volOfVol = data.getVolatilityOfVolatility();
    _rho = data.getCorrelation();
  }
  public double getHalfLife() {
    return _lambda;
  }

  public double getLongRunVolatility() {
    return _sigmaLR;
  }

  public double getVolatilityOfVolatility() {
    return _volOfVol;
  }

  public double getCorrelation() {
    return _rho;
  }

  @Override
  public HullWhiteStochasticVolatilityModelOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelOptionDataBundle withCostOfCarry(final Double b) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), b, getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelOptionDataBundle withSpot(final Double spot) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getHalfLife(),
        getLongRunVolatility(), getVolatilityOfVolatility(), getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelOptionDataBundle withDate(final ZonedDateTime date) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getHalfLife(),
        getLongRunVolatility(), getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelOptionDataBundle withHalfLife(final double lambda) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda,
        getLongRunVolatility(), getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelOptionDataBundle withLongRunVolatility(final double longRunVolatility) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(),
        longRunVolatility, getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelOptionDataBundle withVolatilityOfVolatility(final double volOfVol) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(),
        getLongRunVolatility(), volOfVol, getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelOptionDataBundle withCorrelation(final double rho) {
    return new HullWhiteStochasticVolatilityModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(),
        getLongRunVolatility(), getVolatilityOfVolatility(), rho);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_sigmaLR);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_volOfVol);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final HullWhiteStochasticVolatilityModelOptionDataBundle other = (HullWhiteStochasticVolatilityModelOptionDataBundle) obj;
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_sigmaLR) != Double.doubleToLongBits(other._sigmaLR)) {
      return false;
    }
    if (Double.doubleToLongBits(_volOfVol) != Double.doubleToLongBits(other._volOfVol)) {
      return false;
    }
    return true;
  }
}
