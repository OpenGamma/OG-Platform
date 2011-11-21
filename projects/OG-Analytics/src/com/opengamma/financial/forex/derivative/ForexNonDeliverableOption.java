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
 * Class describing a non-deliverable foreign exchange European option. The option exercise date is the underlying NDF fixing date.
 * When the option is a call, the option holder has the right to enter into the Forex NDF; when the option is a put, the option holder has the right to enter into a NDF 
 * transaction equal to the underlying but with opposite signs. The settlement is done in the second currency of the NDF.
 * A Call on a Forex on KRW / USD at strike 1124.00 is thus the right to receive 1.00 USD and pay 1124.00 KRW and cash settle the difference in USD at the fixing rate. 
 * A put on a Forex on KRW / USD at strike 1124.00 is thus the right to pay 1.00 USD and receive 1124.00 KRW and cash settle the difference in USD at the fixing rate.
 * There is not a full put/call parity in NDO as the two currencies do not have a fully symmetric role.
 */
public class ForexNonDeliverableOption implements InstrumentDerivative {

  /**
   * The underlying Forex transaction (the one entered into in case of exercise).
   * The NDF fixing time is the option exercise time.
   */
  private final ForexNonDeliverableForward _underlyingNDF;
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
   * @param underlyingNDF The underlying Forex transaction (the one entered into in case of exercise).
   * @param isCall The call (true) / put (false) flag.
   * @param isLong The long (true) / short (false) flag.
   */
  public ForexNonDeliverableOption(ForexNonDeliverableForward underlyingNDF, boolean isCall, boolean isLong) {
    Validate.notNull(underlyingNDF, "Underlying NDF is null");
    this._underlyingNDF = underlyingNDF;
    _isLong = isLong;
    _isCall = isCall;
  }

  /**
   * Gets the underlying Forex NDF transaction.
   * @return The underlying transaction.
   */
  public ForexNonDeliverableForward getUnderlyingNDF() {
    return _underlyingNDF;
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
    return _underlyingNDF.getCurrency1();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _underlyingNDF.getCurrency2();
  }

  /**
   * Gets the option strike.
   * @return The strike.
   */
  public double getStrike() {
    return _underlyingNDF.getExchangeRate();
  }

  /**
   * Gets the option time to expiration.
   * @return The time to expiration.
   */
  public double getExpiryTime() {
    return _underlyingNDF.getFixingTime();
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexNonDeliverableOption(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexNonDeliverableOption(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + _underlyingNDF.hashCode();
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
    ForexNonDeliverableOption other = (ForexNonDeliverableOption) obj;
    if (_isCall != other._isCall) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingNDF, other._underlyingNDF)) {
      return false;
    }
    return true;
  }

}
