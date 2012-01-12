/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;

/**
 * Class describing a digital foreign exchange European option. 
 * The implied strike is the absolute value of ratio of the domestic currency (currency 2) amount and the foreign currency amount (currency1).
 * When the option is a call, it pays the absolute value of the domestic currency amount when the spot rate is above the strike and nothing otherwise.
 * When the option is a put, it pays the absolute value of the domestic currency amount when the spot rate is below the strike and nothing otherwise.
 */
public class ForexOptionDigitalDefinition implements InstrumentDefinition<InstrumentDerivative> {

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
  public ForexOptionDigitalDefinition(final ForexDefinition forex, final ZonedDateTime expirationDate, final boolean isCall, boolean isLong) {
    Validate.notNull(forex, "Underlying forex");
    Validate.notNull(expirationDate, "Expiration date");
    Validate.isTrue(!expirationDate.isAfter(forex.getExchangeDate()), "Expiration should be before payment.");
    _underlyingForex = forex;
    _expirationDate = expirationDate;
    _isCall = isCall;
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
   */
  @Override
  public ForexOptionDigital toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yieldCurveNames");
    final Forex fx = _underlyingForex.toDerivative(date, yieldCurveNames);
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double expirationTime = actAct.getDayCountFraction(date, _expirationDate);
    return new ForexOptionDigital(fx, expirationTime, _isCall, _isLong);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitForexOptionDigitalDefinition(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitForexOptionDigitalDefinition(this);
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
    ForexOptionDigitalDefinition other = (ForexOptionDigitalDefinition) obj;
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
