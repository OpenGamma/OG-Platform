/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition.twoasset;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  The standard data required to price two-asset options.
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

  /**
   * 
   * @param interestRateCurve The interest rate curve (expects the x-axis to be time and the y-axis to be rate)
   * @param b1 The cost-of-carry of the first asset
   * @param b2 The cost-of-carry of the second asset
   * @param volatilitySurface1 The volatility surface for the option on the first underlying (expects the x-axis to be time, the y-axis to be strike and the z-axis to be volatility)
   * @param volatilitySurface2 The volatility surface for the option on the second underlying (expects the x-axis to be time, the y-axis to be strike and the z-axis to be volatility)
   * @param spot1 The spot value of the first underlying
   * @param spot2 The spot value of the second underlying
   * @param rho The correlation between the spot rates of the underlying
   * @param date The date of this data
   * @throws IllegalArgumentException If $\rho < -1$ or $\rho > 1$ 
   */
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

  /**
   * Copies the data from another two-asset option data bundle
   * @param other The data bundle
   * @throws IllegalArgumentException If the other data bundle is null
   */
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

  /**
   * Gets the interest rate as a decimal 
   * @param t The time to expiry
   * @return The interest rate
   */
  public double getInterestRate(final double t) {
    return getInterestRateCurve().getInterestRate(t);
  }

  /**
   * Gets the cost of carry of the first asset as a decimal 
   * @return The cost of carry 
   */
  public double getFirstCostOfCarry() {
    return _b1;
  }

  /**
   * Gets the cost of carry of the second asset as a decimal 
   * @return The cost of carry 
   */
  public double getSecondCostOfCarry() {
    return _b2;
  }

  /**
   * Gets the volatility of the first asset as a decimal
   * @param timeToExpiry The time to expiry
   * @param strike The strike 
   * @return The volatility
   */
  public double getFirstVolatility(final double timeToExpiry, final double strike) {
    return getFirstVolatilitySurface().getVolatility(DoublesPair.of(timeToExpiry, strike));
  }

  /**
   * Gets the volatility of the second asset as a decimal
   * @param timeToExpiry The time to expiry
   * @param strike The strike 
   * @return The volatility
   */
  public double getSecondVolatility(final double timeToExpiry, final double strike) {
    return getSecondVolatilitySurface().getVolatility(DoublesPair.of(timeToExpiry, strike));
  }

  /**
   * 
   * @return The spot value of the first asset
   */
  public double getFirstSpot() {
    return _spot1;
  }

  /**
   * 
   * @return The spot value of the second asset
   */
  public double getSecondSpot() {
    return _spot2;
  }

  /**
   * 
   * @return The interest rate curve
   */
  public YieldAndDiscountCurve getInterestRateCurve() {
    return _interestRateCurve;
  }

  /**
   * 
   * @return The volatility surface for the first asset
   */
  public VolatilitySurface getFirstVolatilitySurface() {
    return _volatilitySurface1;
  }

  /**
   * 
   * @return The volatility surface for the second asset
   */
  public VolatilitySurface getSecondVolatilitySurface() {
    return _volatilitySurface2;
  }

  /**
   * 
   * @return The correlation of the spot prices of the first and second asset
   */
  public double getCorrelation() {
    return _rho;
  }

  /**
   * 
   * @return The date for which the data are valid
   */
  public ZonedDateTime getDate() {
    return _date;
  }

  /**
   * Returns a new data bundle with the interest rate curve replaced by the argument
   * @param interestRateCurve The new interest rate curve
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve interestRateCurve) {
    return new StandardTwoAssetOptionDataBundle(interestRateCurve, getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the cost of carry of the first asset replaced by the argument
   * @param costOfCarry The new cost of carry
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withFirstCostOfCarry(final double costOfCarry) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), costOfCarry, getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the cost of carry of the second asset replaced by the argument
   * @param costOfCarry The new cost of carry
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withSecondCostOfCarry(final double costOfCarry) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), costOfCarry, getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(), getSecondSpot(),
        getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the volatility surface of the first asset replaced by the argument
   * @param volatilitySurface The new volatility surface
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withFirstVolatilitySurface(final VolatilitySurface volatilitySurface) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), volatilitySurface, getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the volatility surface of the second asset replaced by the argument
   * @param volatilitySurface The new volatility surface
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withSecondVolatilitySurface(final VolatilitySurface volatilitySurface) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), volatilitySurface, getFirstSpot(), getSecondSpot(),
        getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the spot value of the first asset replaced by the argument
   * @param spot The new spot
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withFirstSpot(final double spot) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), spot,
        getSecondSpot(), getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the spot value of the second asset replaced by the argument
   * @param spot The new spot
   * @return The new data bundle
   */
  public StandardTwoAssetOptionDataBundle withSecondSpot(final double spot) {
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(), spot,
        getCorrelation(), getDate());
  }

  /**
   * Returns a new data bundle with the correlation between the two spot prices replaced by the argument
   * @param correlation The correlation
   * @return The new data bundle
   * @throws IllegalArgumentException If $\rho < -1$ or $\rho > 1$ 
   */
  public StandardTwoAssetOptionDataBundle withCorrelation(final double correlation) {
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, correlation)) {
      throw new IllegalArgumentException("Correlation must be between 0 and 1");
    }
    return new StandardTwoAssetOptionDataBundle(getInterestRateCurve(), getFirstCostOfCarry(), getSecondCostOfCarry(), getFirstVolatilitySurface(), getSecondVolatilitySurface(), getFirstSpot(),
        getSecondSpot(), correlation, getDate());
  }

  /**
   * Returns a new data bundle with the date replaced by the argument
   * @param date The date
   * @return The new data bundle
   */
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
