/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition.twoasset;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class StandardTwoAssetOptionDataBundle {
  private final YieldAndDiscountCurve _interestRateCurve;
  private final VolatilitySurface _volatilitySurface1;
  private final VolatilitySurface _volatilitySurface2;
  private final double _b1;
  private final double _b2;
  private final double _spot1;
  private final double _spot2;
  private final double _rho;
  private final ZonedDateTime _date;

  public StandardTwoAssetOptionDataBundle(final YieldAndDiscountCurve interestRateCurve, final double b1, final double b2, final VolatilitySurface volatilitySurface1,
      final VolatilitySurface volatilitySurface2, final double spot1, final double spot2, final double rho, final ZonedDateTime date) {
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, rho)) {
      throw new IllegalArgumentException("Correlation must be between -1 and 1");
    }
    _interestRateCurve = interestRateCurve;
    _b1 = b1;
    _b2 = b2;
    _volatilitySurface1 = volatilitySurface1;
    _volatilitySurface2 = volatilitySurface2;
    _spot1 = spot1;
    _spot2 = spot2;
    _rho = rho;
    _date = date;
  }

  public StandardTwoAssetOptionDataBundle(final StandardTwoAssetOptionDataBundle other) {
    Validate.notNull(other, "data bundle");
    _interestRateCurve = other.getInterestRateCurve();
    _b1 = other.getFirstCostOfCarry();
    _b2 = other.getSecondCostOfCarry();
    _volatilitySurface1 = other.getFirstVolatilitySurface();
    _volatilitySurface2 = other.getSecondVolatilitySurface();
    _spot1 = other.getFirstSpot();
    _spot2 = other.getSecondSpot();
    _rho = other.getCorrelation();
    _date = other.getDate();
  }

  public double getInterestRate(final double t) {
    return getInterestRateCurve().getInterestRate(t);
  }

  public double getFirstCostOfCarry() {
    return _b1;
  }

  public double getSecondCostOfCarry() {
    return _b2;
  }

  public double getFirstVolatility(final double timeToExpiry, final double strike) {
    return getFirstVolatilitySurface().getVolatility(Pair.of(timeToExpiry, strike));
  }

  public double getSecondVolatility(final double timeToExpiry, final double strike) {
    return getSecondVolatilitySurface().getVolatility(Pair.of(timeToExpiry, strike));
  }

  public double getFirstSpot() {
    return _spot1;
  }

  public double getSecondSpot() {
    return _spot2;
  }

  public YieldAndDiscountCurve getInterestRateCurve() {
    return _interestRateCurve;
  }

  public VolatilitySurface getFirstVolatilitySurface() {
    return _volatilitySurface1;
  }

  public VolatilitySurface getSecondVolatilitySurface() {
    return _volatilitySurface2;
  }

  public double getCorrelation() {
    return _rho;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public StandardTwoAssetOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve interestRateCurve) {
    return new StandardTwoAssetOptionDataBundle(interestRateCurve, getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withFirstCostOfCarry(final double costOfCarry) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), costOfCarry, getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withSecondCostOfCarry(final double costOfCarry) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), costOfCarry, getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(), getSecondSpot(),
        getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withFirstVolatilitySurface(final VolatilitySurface volatilitySurface) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), volatilitySurface, getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withSecondVolatilitySurface(final VolatilitySurface volatilitySurface) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), volatilitySurface, getFirstSpot(), getSecondSpot(),
        getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withFirstSpot(final double spot) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), spot,
        getSecondSpot(), getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withSecondSpot(final double spot) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(), spot,
        getCorrelation(), getDate());
  }

  public StandardTwoAssetOptionDataBundle withCorrelation(final double correlation) {
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, correlation)) {
      throw new IllegalArgumentException("Correlation must be between 0 and 1");
    }
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), correlation, getDate());
  }

  public StandardTwoAssetOptionDataBundle withDate(final ZonedDateTime date) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), date);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_b1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_b2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_interestRateCurve == null) ? 0 : _interestRateCurve.hashCode());
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spot1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spot2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilitySurface1 == null) ? 0 : _volatilitySurface1.hashCode());
    result = prime * result + ((_volatilitySurface2 == null) ? 0 : _volatilitySurface2.hashCode());
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
    final StandardTwoAssetOptionDataBundle other = (StandardTwoAssetOptionDataBundle) obj;
    if (Double.doubleToLongBits(_b1) != Double.doubleToLongBits(other._b1)) {
      return false;
    }
    if (Double.doubleToLongBits(_b2) != Double.doubleToLongBits(other._b2)) {
      return false;
    }
    if (!ObjectUtils.equals(_date, other._date)) {
      return false;
    }
    if (!ObjectUtils.equals(_interestRateCurve, other._interestRateCurve)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_spot1) != Double.doubleToLongBits(other._spot1)) {
      return false;
    }
    if (Double.doubleToLongBits(_spot2) != Double.doubleToLongBits(other._spot2)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilitySurface1, other._volatilitySurface1)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilitySurface2, other._volatilitySurface2)) {
      return false;
    }
    return true;
  }

}
