/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureSecurity {

  /**
   * Future last trading time. Usually the date for which the third Wednesday of the month is the spot date.
   */
  private final double _lastTradingTime;
  /**
   * Ibor index associated to the future.
   */
  private final IborIndex _iborIndex;
  /**
   * Fixing period of the reference Ibor starting time.
   */
  private final double _fixingPeriodStartTime;
  /**
   * Fixing period of the reference Ibor end time.
   */
  private final double _fixingPeriodEndTime;
  /**
   * Fixing period of the reference Ibor accrual factor.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * Future notional.
   */
  private final double _notional;
  /**
   * Future payment accrual factor. Usually a standardized number of 0.25 for a 3M future.
   */
  private final double _paymentAccrualFactor;
  /**
   * Future name.
   */
  private final String _name;
  /**
   * The name of the forward curve used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndIme Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor. 
   * @param forwardCurveName  The name of the forward curve used in to estimate the fixing index.
   * @param name Future name.
   */
  public InterestRateFutureSecurity(double lastTradingTime, IborIndex iborIndex, double fixingPeriodStartTime, double fixingPeriodEndIme, double fixingPeriodAccrualFactor, double notional,
      double paymentAccrualFactor, String forwardCurveName, String name) {
    Validate.notNull(iborIndex, "Ibor index");
    Validate.notNull(forwardCurveName, "Forward curve name");
    Validate.notNull(name, "Name");
    this._lastTradingTime = lastTradingTime;
    this._iborIndex = iborIndex;
    this._fixingPeriodStartTime = fixingPeriodStartTime;
    this._fixingPeriodEndTime = fixingPeriodEndIme;
    this._fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    this._notional = notional;
    this._paymentAccrualFactor = paymentAccrualFactor;
    _forwardCurveName = forwardCurveName;
    this._name = name;
  }

  /**
   * Gets the future last trading time.
   * @return The future last trading time.
   */
  public double getLastTradingTime() {
    return _lastTradingTime;
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index.
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the fixing period of the reference Ibor starting time.
   * @return The fixing period starting time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period of the reference Ibor end time.
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor. 
   * @return The future payment accrual factor. 
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the _forwardCurveName field.
   * @return the _forwardCurveName
   */
  public String getForwardCurveName() {
    return _forwardCurveName;
  }

  /**
   * Gets the future name.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _forwardCurveName.hashCode();
    result = prime * result + _iborIndex.hashCode();
    temp = Double.doubleToLongBits(_lastTradingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _name.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
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
    InterestRateFutureSecurity other = (InterestRateFutureSecurity) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (Double.doubleToLongBits(_lastTradingTime) != Double.doubleToLongBits(other._lastTradingTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    return true;
  }

}
