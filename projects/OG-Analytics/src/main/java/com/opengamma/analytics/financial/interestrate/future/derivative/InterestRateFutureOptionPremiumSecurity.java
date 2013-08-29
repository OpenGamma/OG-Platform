/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future option with up-front margin security.
 */
public class InterestRateFutureOptionPremiumSecurity implements InstrumentDerivative {

  /**
   * Underlying future security.
   */
  private final InterestRateFutureSecurity _underlyingFuture;
  /**
   * Expiration date.
   */
  private final double _expirationTime;
  /**
   * Cap (true) / floor (false) flag.
   */
  private final boolean _isCall;
  /**
   * Strike price.
   */
  private final double _strike;
  /**
   * The discounting curve name.
   */
  private String _discountingCurveName;
  /**
   * The forward curve name used in to estimate the fixing index.
   */
  private String _forwardCurveName;

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying future security.
   * @param expirationTime The time (in year) to expiration.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  @SuppressWarnings("deprecation")
  public InterestRateFutureOptionPremiumSecurity(final InterestRateFutureSecurity underlyingFuture, final double expirationTime, final double strike, final boolean isCall) {
    ArgumentChecker.notNull(underlyingFuture, "underlying future");
    _underlyingFuture = underlyingFuture;
    _expirationTime = expirationTime;
    _strike = strike;
    _isCall = isCall;
    try {
      _discountingCurveName = underlyingFuture.getDiscountingCurveName();
      _forwardCurveName = underlyingFuture.getForwardCurveName();
    } catch (final IllegalStateException e) {
      _discountingCurveName = null;
      _forwardCurveName = null;
    }
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future security.
   */
  public InterestRateFutureSecurity getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the expiration date.
   * @return The expiration date.
   */
  public double getExpirationTime() {
    return _expirationTime;
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

  /**
   * Gets the discounting curve name.
   * @return The discounting curve name.
   * @deprecated Curve names should not be set in derivatives
   */
  @Deprecated
  public String getDiscountingCurveName() {
    if (_discountingCurveName == null) {
      throw new IllegalStateException("Curve names should not be set in derivatives");
    }
    return _discountingCurveName;
  }

  /**
   * Gets the forward curve name.
   * @return The forward curve name.
   * @deprecated Curve names should not be set in derivatives
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_forwardCurveName == null) {
      throw new IllegalStateException("Curve names should not be set in derivatives");
    }
    return _forwardCurveName;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_discountingCurveName == null ? 0 : _discountingCurveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_expirationTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_forwardCurveName == null ? 0 : _forwardCurveName.hashCode());
    result = prime * result + (_isCall ? 1231 : 1237);
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
    final InterestRateFutureOptionPremiumSecurity other = (InterestRateFutureOptionPremiumSecurity) obj;
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_expirationTime) != Double.doubleToLongBits(other._expirationTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
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
