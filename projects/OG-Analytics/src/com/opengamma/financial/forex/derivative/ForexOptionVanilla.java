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
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.money.Currency;

/**
 * Class describing a vanilla foreign exchange European option. When the option is a call, the option holder has the right to enter into the Forex transaction; 
 * if the option is a put, the option holder has the right to enter into a Forex transaction equal to the underlying but with opposite signs.
 * A Call on a Forex EUR 1.00 / USD -1.41 is thus the right to call 1.00 EUR and put 1.41 USD. A put on a Forex EUR -1.00 / USD 1.41 is the right to 
 * exchange -(-1.00) EUR = 1.00 EUR and -1.41 EUR; it is thus also the right to call 1.00 EUR and put 1.41 USD. A put on a Forex  USD 1.41 / EUR -1.00 is 
 * also the right to call 1.00 EUR and put 1.41 USD.
 */
public class ForexOptionVanilla extends EuropeanVanillaOption implements ForexDerivative {

  /**
   * The underlying Forex transaction (the one entered into in case of exercise).
   */
  private final Forex _underlyingForex;
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
  public ForexOptionVanilla(Forex underlyingForex, double expirationTime, boolean isCall, boolean isLong) {
    super(-underlyingForex.getPaymentCurrency2().getAmount() / underlyingForex.getPaymentCurrency1().getAmount(), expirationTime, isCall ^ (underlyingForex.getPaymentCurrency1().getAmount() < 0));
    Validate.isTrue(expirationTime <= underlyingForex.getPaymentTime(), "Expiration should be before payment.");
    this._underlyingForex = underlyingForex;
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

  @Override
  public <S, T> T accept(ForexDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexOptionVanilla(this, data);
  }

  @Override
  public <T> T accept(ForexDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexOptionVanilla(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + _underlyingForex.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    ForexOptionVanilla other = (ForexOptionVanilla) obj;
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingForex, other._underlyingForex)) {
      return false;
    }
    return true;
  }

}
