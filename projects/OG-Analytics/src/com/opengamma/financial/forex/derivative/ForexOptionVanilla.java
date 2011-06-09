/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.ForexDerivativeVisitor;

/**
 * Class describing a vanilla foreign exchange option. When the option is a call, the option holder has the right to enter into the Forex transaction; 
 * if the option is a put, the option holder has the right to enter into a Forex transaction equal to the underlying but with opposite signs.
 * A Call on a Forex EUR 1.00 / USD -1.41 is thus the right to call 1.00 EUR and put 1.41 USD. A put on a Forex EUR -1.00 / USD 1.41 is the right to 
 * exchange -(-1.00) EUR = 1.00 EUR and -1.41 EUR; it is thus also the right to call 1.00 EUR and put 1.41 USD. A put on a Forex  USD 1.41 / EUR -1.00 is 
 * also the right to call 1.00 EUR and put 1.41 USD.
 */
public class ForexOptionVanilla implements ForexDerivative {

  /**
   * The underlying Forex transaction (the one entered into in case of exercise).
   */
  private final Forex _underlyingForex;
  /**
   * The expiration date (and time) of the option.
   */
  private final double _expirationTime;
  /**
   * The call (true) / put (false) flag.
   */
  private final boolean _isCall;

  /**
   * Constructor from all details.
   * @param underlyingForex The underlying Forex transaction (the one entered into in case of exercise).
   * @param expirationTime The expiration date (and time) of the option.
   * @param isCall The call (true) / put (false) flag.
   */
  public ForexOptionVanilla(Forex underlyingForex, double expirationTime, boolean isCall) {
    Validate.notNull(underlyingForex, "Underlying forex");
    Validate.isTrue(expirationTime <= underlyingForex.getPaymentTime(), "Expiration should be before payment.");
    this._underlyingForex = underlyingForex;
    this._expirationTime = expirationTime;
    this._isCall = isCall;
  }

  /**
   * Gets the underlying Forex transaction.
   * @return The underlying Forex transaction.
   */
  public Forex getUnderlyingForex() {
    return _underlyingForex;
  }

  /**
   * Gets the expiration date (and time) of the option.
   * @return The expiration date.
   */
  public double getExpirationTime() {
    return _expirationTime;
  }

  /**
   * Gets the call (true) / put (false) flag.
   * @return The call / put flag.
   */
  public boolean isCall() {
    return _isCall;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_expirationTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + _underlyingForex.hashCode();
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
    ForexOptionVanilla other = (ForexOptionVanilla) obj;
    if (Double.doubleToLongBits(_expirationTime) != Double.doubleToLongBits(other._expirationTime)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingForex, other._underlyingForex)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(ForexDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexOptionVanilla(this, data);
  }

  @Override
  public <T> T accept(ForexDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexOptionVanilla(this);
  }

}
