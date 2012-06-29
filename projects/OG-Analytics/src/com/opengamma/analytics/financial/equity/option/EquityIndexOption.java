/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.EquityDerivativeVisitor;
import com.opengamma.util.money.Currency;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * OG-Analytics derivative of the exchange traded Equity Index Option
 */
public class EquityIndexOption implements EquityDerivative {
  private final double _timeToExpiry;
  private final double _timeToSettlement;
  private final double _strike;
  private final boolean _isCall;
  private final double _unitAmount;
  private final Currency _currency;

  /**
   * 
   * @param timeToExpiry    time (in years as a double) until the date-time at which the reference index is fixed  
   * @param timeToSettlement  time (in years as a double) until the date-time at which the contract is settled
   * @param strike         Set strike price at trade time. Note that we may handle margin by resetting this at the end of each trading day
   * @param isCall         True if the option is a Call, false if it is a Put.
   * @param currency       The reporting currency of the future
   *  @param unitAmount    The unit value per tick, in given currency  
   */
  public EquityIndexOption(final double timeToExpiry,
                        final double timeToSettlement,
                        final double strike,
                        final boolean isCall,
                        final Currency currency,
                        final double unitAmount) {
    Validate.isTrue(unitAmount > 0, "point value must be positive");
    _timeToExpiry = timeToExpiry;
    _timeToSettlement = timeToSettlement;
    _strike = strike;
    _isCall = isCall;
    _unitAmount = unitAmount;
    _currency = currency;
  }

  /** 
   * @return the fixing date (in years as a double)
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Gets the date when payments are made 
   * @return the delivery date (in years as a double)
   */
  @Override
  public double getTimeToSettlement() {
    return _timeToSettlement;
  }

  /**
   * @return the strike of the option.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @return True if the option is a Call, false if it is a Put.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the unit amount, this is the notional of a single security.
   * @return the point value
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  @Override
  /// @export "accept-visitor"
  public <S, T> T accept(final EquityDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitEquityIndexOption(this, data);
  }

  /// @end
  @Override
  public <T> T accept(final EquityDerivativeVisitor<?, T> visitor) {
    return visitor.visitEquityIndexOption(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_timeToSettlement);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_timeToExpiry);
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
    EquityIndexOption other = (EquityIndexOption) obj;

    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }

    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToSettlement) != Double.doubleToLongBits(other._timeToSettlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    return true;

  }

}
