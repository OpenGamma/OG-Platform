/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a digital foreign exchange European option.
 * The implied strike is the absolute value of ratio of the domestic currency (currency 2) amount and the foreign currency amount (currency1).
 * When the option is a call, it pays the absolute value of the payment currency amount when the spot rate is above the strike and nothing otherwise.
 * When the option is a put, it pays the absolute value of the payment currency amount when the spot rate is below the strike and nothing otherwise.
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
   * The flag indicating which currency is paid. If true, the domestic currency amount is paid, if false, the foreign currency amount is paid.
   */
  private final boolean _payDomestic;

  // TODO: review description. Should we store the strike explicitly?

  /**
   * Constructor from the details. The default payment currency is domestic/currency 2.
   * @param forex The underlying Forex transaction.
   * @param expirationDate The expiration date (and time) of the option.
   * @param isCall The call (true) / put (false) flag.
   * @param isLong The long (true) / short (false) flag.
   */
  public ForexOptionDigitalDefinition(final ForexDefinition forex, final ZonedDateTime expirationDate, final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(forex, "Underlying forex");
    ArgumentChecker.notNull(expirationDate, "Expiration date");
    ArgumentChecker.isTrue(!expirationDate.isAfter(forex.getExchangeDate()), "Expiration should be before payment.");
    _underlyingForex = forex;
    _expirationDate = expirationDate;
    _isCall = isCall;
    _isLong = isLong;
    _payDomestic = true;
  }

  /**
   * Constructor from the details. The default payment currency is domestic/currency 2.
   * @param forex The underlying Forex transaction.
   * @param expirationDate The expiration date (and time) of the option.
   * @param isCall The call (true) / put (false) flag.
   * @param isLong The long (true) / short (false) flag.
   * @param payDomestic The flag indicating which currency is paid. If true, the domestic currency amount is paid, if false, the foreign currency amount is paid.
   */
  public ForexOptionDigitalDefinition(final ForexDefinition forex, final ZonedDateTime expirationDate, final boolean isCall, final boolean isLong, final boolean payDomestic) {
    ArgumentChecker.notNull(forex, "Underlying forex");
    ArgumentChecker.notNull(expirationDate, "Expiration date");
    ArgumentChecker.isTrue(!expirationDate.isAfter(forex.getExchangeDate()), "Expiration should be before payment.");
    _underlyingForex = forex;
    _expirationDate = expirationDate;
    _isCall = isCall;
    _isLong = isLong;
    _payDomestic = payDomestic;
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
   * Gets the flag indicating which currency is paid. If true, the domestic currency amount is paid, if false, the foreign currency amount is paid.
   * @return The payment currency flag.
   */
  public boolean payDomestic() {
    return _payDomestic;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public ForexOptionDigital toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ForexOptionDigital toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final Forex fx = _underlyingForex.toDerivative(date);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double expirationTime = actAct.getDayCountFraction(date, _expirationDate);
    return new ForexOptionDigital(fx, expirationTime, _isCall, _isLong, _payDomestic);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionDigitalDefinition(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionDigitalDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expirationDate.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + (_payDomestic ? 1231 : 1237);
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
    final ForexOptionDigitalDefinition other = (ForexOptionDigitalDefinition) obj;
    if (!ObjectUtils.equals(_expirationDate, other._expirationDate)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (_payDomestic != other._payDomestic) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingForex, other._underlyingForex)) {
      return false;
    }
    return true;
  }

}
