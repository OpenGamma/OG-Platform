/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future option security with premium paid up-front (CME type). The option is of American type.
 */
public class InterestRateFutureOptionPremiumSecurityDefinition implements InstrumentDefinition<InterestRateFutureOptionPremiumSecurity> {

  /**
   * Underlying future security.
   */
  private final InterestRateFutureSecurityDefinition _underlyingFuture;
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
   * @param isCall The cap (true) / floor (false) flag.
   */
  public InterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureSecurityDefinition underlyingFuture, final ZonedDateTime expirationDate, final double strike, final boolean isCall) {
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
  public InterestRateFutureSecurityDefinition getUnderlyingFuture() {
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
   * The future option currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingFuture.getCurrency();
  }

  @Override
  public InterestRateFutureOptionPremiumSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(_expirationDate), "Date is after expiration date");
    final InterestRateFutureSecurity underlyingFuture = _underlyingFuture.toDerivative(date);
    final double expirationTime = TimeCalculator.getTimeBetween(date, _expirationDate);
    return new InterestRateFutureOptionPremiumSecurity(underlyingFuture, expirationTime, _strike, _isCall);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumSecurityDefinition(this);
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
    final InterestRateFutureOptionPremiumSecurityDefinition other = (InterestRateFutureOptionPremiumSecurityDefinition) obj;
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
