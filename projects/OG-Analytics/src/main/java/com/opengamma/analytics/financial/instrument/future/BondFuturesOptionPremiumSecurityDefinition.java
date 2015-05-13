/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an bond future option security with premium paid up-front (CBOT type). The option is of American type.
 */
public class BondFuturesOptionPremiumSecurityDefinition implements InstrumentDefinition<BondFuturesOptionPremiumSecurity> {

  /**
   * Underlying future security.
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
   * @param underlyingFuture The underlying future security.
   * @param expirationDate The expiration date.
   * @param strike The option strike.
   * @param isCall The call (true) / put (false) flag.
   */
  public BondFuturesOptionPremiumSecurityDefinition(final BondFuturesSecurityDefinition underlyingFuture, 
      final ZonedDateTime expirationDate, final double strike, final boolean isCall) {
    ArgumentChecker.notNull(underlyingFuture, "underlying future");
    ArgumentChecker.notNull(expirationDate, "expiration");
    _underlyingFuture = underlyingFuture;
    _expirationDate = expirationDate;
    _strike = strike;
    _isCall = isCall;
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future security.
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
   * Gets the notional.
   * @return The notional.
   */
  public double getNotional() {
    return _underlyingFuture.getNotional();
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
   * The future option currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingFuture.getCurrency();
  }

  @Override
  public BondFuturesOptionPremiumSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(_expirationDate), "Date is after expiration date");
    final BondFuturesSecurity underlyingFuture = _underlyingFuture.toDerivative(date);
    final double expirationTime = TimeCalculator.getTimeBetween(date, _expirationDate);
    return new BondFuturesOptionPremiumSecurity(underlyingFuture, expirationTime, _strike, _isCall);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureOptionPremiumSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureOptionPremiumSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFuturesOptionPremiumSecurityDefinition other = (BondFuturesOptionPremiumSecurityDefinition) obj;
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
