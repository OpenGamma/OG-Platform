/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFuture implements InterestRateDerivative {
  private final double _settlement;
  private final double _yearFraction;
  private final double _price;
  private final String _curveName;

  /**
   * Set up for a Euro-Dollar future
   * @param settlementDate time in years from today to settlement of future (normally the third Wednesday of March, June, September & December for 3m contracts, but any positive value is allowed)
   * @param yearFraction length (in years) of hypothetical deposit rate - for 3 month Libor this is 0.25 = 90/360
   * @param price quoted price of the future, price = 100*(1-r), where r is the implied futures rate as a fraction 
   * @param yieldCurveName The name of the curve used to calculate the reference rate 
   */
  public InterestRateFuture(final double settlementDate, final double yearFraction, final double price, final String yieldCurveName) {
    ArgumentChecker.notNegative(settlementDate, "start time");
    ArgumentChecker.notNegative(yearFraction, "year fraction");
    ArgumentChecker.notNegative(price, "price");
    Validate.isTrue(price <= 100, "price must be less thatn 100");
    Validate.notNull(yieldCurveName);

    _curveName = yieldCurveName;
    _settlement = settlementDate;
    _price = price;
    _yearFraction = yearFraction;
  }

  public double getSettlementDate() {
    return _settlement;
  }

  public double getYearFraction() {
    return _yearFraction;
  }

  public double getPrice() {
    return _price;
  }

  public String getCurveName() {
    return _curveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yearFraction);
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
    InterestRateFuture other = (InterestRateFuture) obj;
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_price) != Double.doubleToLongBits(other._price)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlement) != Double.doubleToLongBits(other._settlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_yearFraction) != Double.doubleToLongBits(other._yearFraction)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitInterestRateFuture(this, curves);
  }

}
