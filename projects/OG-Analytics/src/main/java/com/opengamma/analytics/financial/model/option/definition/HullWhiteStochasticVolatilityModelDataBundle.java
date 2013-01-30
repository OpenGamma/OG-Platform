/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class HullWhiteStochasticVolatilityModelDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _sigmaLR;
  private final double _volOfVol;
  private final double _rho;

  public HullWhiteStochasticVolatilityModelDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date, final double lambda, final double sigmaLR, final double volOfVol, final double rho) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _sigmaLR = sigmaLR;
    _volOfVol = volOfVol;
    _rho = rho;
  }

  public HullWhiteStochasticVolatilityModelDataBundle(final HullWhiteStochasticVolatilityModelDataBundle data) {
    super(data);
    _lambda = data.getHalfLife();
    _sigmaLR = data.getLongRunVolatility();
    _volOfVol = data.getVolatilityOfVolatility();
    _rho = data.getCorrelation();
  }

  public HullWhiteStochasticVolatilityModelDataBundle(final StandardOptionDataBundle data, final double lambda, final double sigmaLR, final double volOfVol, final double rho) {
    super(data);
    _lambda = lambda;
    _sigmaLR = sigmaLR;
    _volOfVol = volOfVol;
    _rho = rho;
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
  public HullWhiteStochasticVolatilityModelDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new HullWhiteStochasticVolatilityModelDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(), getVolatilityOfVolatility(),
        getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelDataBundle withCostOfCarry(final double b) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), b, getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(), getVolatilityOfVolatility(),
        getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getHalfLife(), getLongRunVolatility(), getVolatilityOfVolatility(),
        getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelDataBundle withSpot(final double spot) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  @Override
  public HullWhiteStochasticVolatilityModelDataBundle withDate(final ZonedDateTime date) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelDataBundle withHalfLife(final double lambda) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda, getLongRunVolatility(),
        getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelDataBundle withLongRunVolatility(final double longRunVolatility) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), longRunVolatility,
        getVolatilityOfVolatility(), getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelDataBundle withVolatilityOfVolatility(final double volOfVol) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(), volOfVol,
        getCorrelation());
  }

  public HullWhiteStochasticVolatilityModelDataBundle withCorrelation(final double rho) {
    return new HullWhiteStochasticVolatilityModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getHalfLife(), getLongRunVolatility(),
        getVolatilityOfVolatility(), rho);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigmaLR);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volOfVol);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final HullWhiteStochasticVolatilityModelDataBundle other = (HullWhiteStochasticVolatilityModelDataBundle) obj;
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
