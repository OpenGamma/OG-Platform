/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a digital foreign exchange European option. 
 * The implied strike is the absolute value of ratio of the domestic currency (currency 2) amount and the foreign currency amount (currency1).
 * When the option is a call, it pays the absolute value of the domestic currency amount when the spot rate is above the strike and nothing otherwise.
 * When the option is a put, it pays the absolute value of the domestic currency amount when the spot rate is below the strike and nothing otherwise.
 */
public class ForexOptionDigital implements InstrumentDerivative {

  /**
   * The underlying Forex transaction (the one entered into in case of exercise).
   */
  private final Forex _underlyingForex;
  /**
   * The time to expiration of the option.
   */
  private final double _expirationTime;
  /**
   * The call (true) / put (false) flag.
   */
  private final boolean _isCall;
  /**
   * The long (true) / short (false) flag.
   */
  private final boolean _isLong;

  /**
   * Constructor from all details.
   * @param underlyingForex The underlying Forex transaction (the one entered into in case of exercise).
   * @param expirationTime The expiration date (and time) of the option.
   * @param isCall The call (true) / put (false) flag.
   * @param isLong The long (true) / short (false) flag.
   */
  public ForexOptionDigital(Forex underlyingForex, double expirationTime, boolean isCall, boolean isLong) {
    Validate.notNull(underlyingForex, "Option FX underlying");
    Validate.isTrue(expirationTime <= underlyingForex.getPaymentTime(), "Expiration should be before payment.");
    _underlyingForex = underlyingForex;
    _expirationTime = expirationTime;
    _isCall = isCall;
    _isLong = isLong;
  }

  /**
   * Gets the underlying Forex transaction.
   * @return The underlying Forex transaction.
   */
  public Forex getUnderlyingForex() {
    return _underlyingForex;
  }

  /**
   * Gets the expiration time of the option.
   * @return The time.
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

  /**
   * Gets the long (true) / short (false) flag.
   * @return The long / short flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the first currency.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _underlyingForex.getCurrency1();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _underlyingForex.getCurrency2();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public double getStrike() {
    return -_underlyingForex.getPaymentCurrency2().getAmount() / _underlyingForex.getPaymentCurrency1().getAmount();

  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexOptionDigital(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexOptionDigital(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_expirationTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
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
    ForexOptionDigital other = (ForexOptionDigital) obj;
    if (Double.doubleToLongBits(_expirationTime) != Double.doubleToLongBits(other._expirationTime)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingForex, other._underlyingForex)) {
      return false;
    }
    return true;
  }

}
