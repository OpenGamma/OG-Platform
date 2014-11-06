/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a vanilla foreign exchange European option. When the option is a call, the option holder has the right to enter into the Forex transaction;
 * if the option is a put, the option holder has the right to enter into a Forex transaction equal to the underlying but with opposite signs.
 * A Call on a Forex EUR 1.00 / USD -1.41 is thus the right to call 1.00 EUR and put 1.41 USD. A put on a Forex EUR -1.00 / USD 1.41 is the right to
 * exchange -(-1.00) EUR = 1.00 EUR and -1.41 EUR; it is thus also the right to call 1.00 EUR and put 1.41 USD. A put on a Forex  USD 1.41 / EUR -1.00 is
 * also the right to call 1.00 EUR and put 1.41 USD.
 */
public class ForexOptionVanillaDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * The underlying Forex transaction (the one entered into in case of exercise).
   */
  private final ForexDefinition _underlyingForex;
  /**
   * The expiration date (and time) of the option.
   */
  private final ZonedDateTime _expirationDate;
  /**
   * The call (true) / put (false) flag.
   */
  private final boolean _isCall;
  /**
   * The long (true) / short (false) flag.
   */
  private final boolean _isLong;

  /**
   * Constructor from the details.
   * @param forex The underlying Forex transaction.
   * @param expirationDate The expiration date (and time) of the option.
   * @param isCall The call (true) / put (false) flag.
   * @param isLong The long (true) / short (false) flag.
   */
  public ForexOptionVanillaDefinition(final ForexDefinition forex, final ZonedDateTime expirationDate, final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(forex, "Underlying forex");
    ArgumentChecker.notNull(expirationDate, "Expiration date");
    ArgumentChecker.isTrue(!expirationDate.isAfter(forex.getExchangeDate()), "Expiration should be before payment.");
    this._underlyingForex = forex;
    this._expirationDate = expirationDate;
    this._isCall = isCall;
    _isLong = isLong;
  }

  /**
   * Gets the underlying Forex transaction.
   * @return The underlying Forex transaction.
   */
  public ForexDefinition getUnderlyingForex() {
    return _underlyingForex;
  }

  /**
   * Gets the expiration date (and time) of the option.
   * @return The expiration date.
   */
  public ZonedDateTime getExpirationDate() {
    return _expirationDate;
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
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public ForexOptionVanilla toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ForexOptionVanilla toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final Forex fx = _underlyingForex.toDerivative(date);
    final double expirationTime = TimeCalculator.getTimeBetween(date, _expirationDate);
    return new ForexOptionVanilla(fx, expirationTime, _isCall, _isLong);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionVanillaDefinition(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionVanillaDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expirationDate.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + _underlyingForex.hashCode();
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
    final ForexOptionVanillaDefinition other = (ForexOptionVanillaDefinition) obj;
    if (!ObjectUtils.equals(_expirationDate, other._expirationDate)) {
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
