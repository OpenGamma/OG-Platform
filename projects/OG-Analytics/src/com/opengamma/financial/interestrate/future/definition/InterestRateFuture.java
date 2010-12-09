/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;

/**
 * 
 */
public class InterestRateFuture implements InterestRateDerivativeWithRate {
  private final double _settlement;
  private final double _fixingDate;
  private final double _maturity;
  private final double _indexYearFraction;
  private final double _valueYearFraction;
  private final double _price;
  private final String _curveName;

  /**
   * Basic setup for interest rate future. The fixing date is assumed equal to the settlement date, and the index and value year fractions are assumed equal.
   * @param settlement The date-time (in years as a double)  at which the reference rate is fixed and the future is cash settled  
   * @param maturity The maturity of the reference rate (in years as a double)
   * @param yearFraction The year fraction to used for calculating the reference rate and price sensitivity 
   * @param price quoted price of the future, price = 100*(1-r), where r is the implied futures rate as a fraction 
   * @param indexCurveName The name of the curve used to calculate the reference rate 
   */
  public InterestRateFuture(final double settlement, final double maturity, final double yearFraction, final double price, final String indexCurveName) {
    this(settlement, settlement, maturity, yearFraction, yearFraction, price, indexCurveName);
  }

  /**
   * Setup for a general interest rate future. This follows the Eurodollar futures system of having a quoted price of 100(1-r)  where r is the rate for a hypothetical loan on a some
   * index rate (normal a Libor rate)
   * @param settlement The date-time (in years as a double) at which the future is cash settled (for Eurodollars this is two days before the fixing date, unless that is a bank holiday)
   * @param fixingTime The data-time (in years as a double) when the reference rate is set (normally the third Wednesday of March, June, September & December for 3m contracts,
   * but any positive value is allowed)
   * @param maturity The maturity of the reference rate (in years as a double)
   * @param indexYearFraction The year fraction to used for calculating the reference rate
   * @param valueYearFraction The change in value (per unit notional) for a unit charge in reference rate (this is 0.25 for a Eurodollar future, i.e. $25 for a 1bp change on a $1M notional)
   * @param price  The quoted price of the future, price = 100*(1-r), where r is the implied futures rate as a fraction 
   * @param indexCurveName The name of the curve used to calculate the reference rate 
   */
  public InterestRateFuture(final double settlement, final double fixingTime, final double maturity, final double indexYearFraction, final double valueYearFraction, final double price,
      final String indexCurveName) {
    Validate.isTrue(settlement >= 0, "Settlement Date is negative");
    Validate.isTrue(fixingTime >= 0, "Fixing Date is negative");
    Validate.isTrue(maturity >= 0, "maturity is negative");
    Validate.isTrue(indexYearFraction >= 0, "index year fraction is negative");
    Validate.isTrue(valueYearFraction >= 0, "Value year fraction is negative");
    Validate.isTrue(price >= 0, "price");
    Validate.isTrue(price <= 100, "price must be less than 100");
    Validate.notNull(indexCurveName);
    Validate.isTrue(settlement >= fixingTime, "settlement must be after or at fixing Date");
    Validate.isTrue(maturity > fixingTime, "maturity must be after fixing date");
    _settlement = settlement;
    _fixingDate = fixingTime;
    _maturity = maturity;
    _indexYearFraction = indexYearFraction;
    _valueYearFraction = valueYearFraction;
    _price = price;
    _curveName = indexCurveName;
  }

  /**
   * Gets the date at which the future is cash settled 
   * @return the settlementDate (in years as a double)
   */
  public double getSettlementDate() {
    return _settlement;
  }

  /**
   * Gets the date when the reference rate is set 
   * @return the fixingDate (in years as a double)
   */
  public double getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets the maturity of the reference rate
   * @return the maturity (in years as a double)
   */
  public double getMaturity() {
    return _maturity;
  }

  /**
   * Gets the year fraction used for calculating the reference rate
   * @return the indexYearFraction
   */
  public double getIndexYearFraction() {
    return _indexYearFraction;
  }

  /**
   * Gets the year fraction used for calculating price change 
   * @return the valueYearFraction
   */
  public double getValueYearFraction() {
    return _valueYearFraction;
  }

  /**
   * Get the price when the contract was made 
   * @return The price
   */
  public double getPrice() {
    return _price;
  }

  /**
   * Gets the name of the Yield curve used to calculate the reference rate
   * @return Curve name as a String
   */
  public String getCurveName() {
    return _curveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingDate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_indexYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_maturity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_valueYearFraction);
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
    final InterestRateFuture other = (InterestRateFuture) obj;
    if (!ObjectUtils.equals(_curveName, other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingDate) != Double.doubleToLongBits(other._fixingDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexYearFraction) != Double.doubleToLongBits(other._indexYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_maturity) != Double.doubleToLongBits(other._maturity)) {
      return false;
    }
    if (Double.doubleToLongBits(_price) != Double.doubleToLongBits(other._price)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlement) != Double.doubleToLongBits(other._settlement)) {
      return false;
    }
    return Double.doubleToLongBits(_valueYearFraction) == Double.doubleToLongBits(other._valueYearFraction);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitInterestRateFuture(this, data);
  }

  @Override
  public InterestRateFuture withRate(final double rate) {
    return new InterestRateFuture(getSettlementDate(), getFixingDate(), getMaturity(), getIndexYearFraction(), getValueYearFraction(), 100 * (1 - rate), getCurveName());
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitInterestRateFuture(this);
  }

}
