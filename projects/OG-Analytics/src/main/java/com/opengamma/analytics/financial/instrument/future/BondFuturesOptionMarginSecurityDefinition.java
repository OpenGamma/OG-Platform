/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future option security with daily margining process (LIFFE and Eurex type). The option is of American type.
 */
public class BondFuturesOptionMarginSecurityDefinition extends FuturesSecurityDefinition<BondFuturesOptionMarginSecurity> {

  /**
   * Underlying bond future security.
   */
  private final BondFuturesSecurityDefinition _underlyingFuture;
  /**
   * Expiration date.
   */
  private final ZonedDateTime _expirationDate;
  /**
   * Cap (true) / floor (false) flag.
   */
  private final boolean _isCall;
  /**
   * Strike price.
   */
  private final double _strike;

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying bond futures security.
   * @param lastTradingDate The option last trading date.
   * @param expirationDate The expiration date.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  public BondFuturesOptionMarginSecurityDefinition(final BondFuturesSecurityDefinition underlyingFuture, final ZonedDateTime lastTradingDate,
      final ZonedDateTime expirationDate, final double strike, final boolean isCall) {
    super(lastTradingDate);
    ArgumentChecker.notNull(underlyingFuture, "underlying future");
    ArgumentChecker.notNull(expirationDate, "expiration");
    _underlyingFuture = underlyingFuture;
    _expirationDate = expirationDate;
    _strike = strike;
    _isCall = isCall;
  }

  /**
   * Gets the underlying bond futures security.
   * @return The underlying futures security.
   */
  public BondFuturesSecurityDefinition getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the expiration date.
   * @return The expiration date.
   */
  public ZonedDateTime getExpirationDate() {
    return _expirationDate;
  }

  /**
   * Gets the cap (true) / floor (false) flag.
   * @return The cap/floor flag.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the option strike.
   * @return The option strike.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesOptionMarginSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("toDerivative not implemented with yield curve names for bond futures options.");
  }

  @Override
  public BondFuturesOptionMarginSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final double expirationTime = TimeCalculator.getTimeBetween(date, _expirationDate);
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final BondFuturesSecurity underlyingFuture = _underlyingFuture.toDerivative(date);
    final BondFuturesOptionMarginSecurity option = new BondFuturesOptionMarginSecurity(underlyingFuture, lastTradingTime, expirationTime, _strike, _isCall);
    return option;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFuturesOptionMarginSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFuturesOptionMarginSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _expirationDate.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFuturesOptionMarginSecurityDefinition other = (BondFuturesOptionMarginSecurityDefinition) obj;
    if (!ObjectUtils.equals(_expirationDate, other._expirationDate)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}
