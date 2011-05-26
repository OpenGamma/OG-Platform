/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.EquityDerivative;
import com.opengamma.financial.equity.EquityDerivativeVisitor;

/**
 * 
 */
public class EquityFuture implements EquityDerivative {
  private final double _timeToFixing;
  private final double _timeToDelivery;
  private final double _strike;
  private final double _pointValue;
  private final int _numContracts;
  private final String _assetName;

  /**
   * Skeleton. Needs to be described in full
   * @param timeToFixing    date-time (in years as a double)  at which the reference index is fixed  
   * @param timeToDelivery  date-time (in years as a double)  of settlement
   * @param strike        Set strike price at trade time. Note that we may handle margin by resetting this at the end of each trading day
   * @param pointValue    The unit value per tick, in given currency  
   * @param numContracts    The number of contracts bought or sold. If sold, this will be a negative integer. 
   * @param assetName     Market reference to the underlying dividend index
   */

  public EquityFuture(final double timeToFixing,
                      final double timeToDelivery,
                      final double strike,
                      final double pointValue,
                      int numContracts, final String assetName) {
    Validate.isTrue(pointValue > 0, "point value must be positive");
    Validate.notNull(assetName, "asset name");
    _timeToFixing = timeToFixing;
    _timeToDelivery = timeToDelivery;
    _strike = strike;
    _pointValue = pointValue;
    _numContracts = numContracts;
    _assetName = assetName;
  }

  /**
   * Gets the date when the reference rate is set 
   * @return the fixing date (in years as a double)
   */
  public double getTimeToFixing() {
    return _timeToFixing;
  }

  /**
   * Gets the date when payments are made 
   * @return the delivery date (in years as a double)
   */
  public double getTimeToDelivery() {
    return _timeToDelivery;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the point value.
   * @return the point value
   */
  public double getPointValue() {
    return _pointValue;
  }

  /**
   * Gets the numContracts.
   * @return the numContracts
   */
  public int getNumContracts() {
    return _numContracts;
  }

  /**
   * Gets the assetName.
   * @return the assetName
   * !!! Could we perhaps use this identifier to get information such as i) currency ii) daysToSpot ???
   */
  public String getAssetName() {
    return _assetName;
  }

  @Override
  /// @export "accept-visitor"
  public <S, T> T accept(final EquityDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitEquityFuture(this, data);
  }

  /// @end
  @Override
  public <T> T accept(final EquityDerivativeVisitor<?, T> visitor) {
    return visitor.visitEquityFuture(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _assetName.hashCode();
    result = prime * result + _numContracts;
    long temp;
    temp = Double.doubleToLongBits(_pointValue);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_timeToDelivery);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_timeToFixing);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    EquityFuture other = (EquityFuture) obj;
    if (!ObjectUtils.equals(_assetName, other._assetName)) {
      return false;
    }
    if (_numContracts != other._numContracts) {
      return false;
    }
    if (Double.doubleToLongBits(_pointValue) != Double.doubleToLongBits(other._pointValue)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToDelivery) != Double.doubleToLongBits(other._timeToDelivery)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToFixing) != Double.doubleToLongBits(other._timeToFixing)) {
      return false;
    }
    return true;

  }
}
