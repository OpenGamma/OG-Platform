/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future option security with up-front payment of the premium.
 */
public class InterestRateFutureOptionPremiumSecurity extends InterestRateFutureOptionSecurity {

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying future security.
   * @param expirationTime The time (in year) to expiration.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  public InterestRateFutureOptionPremiumSecurity(InterestRateFutureSecurity underlyingFuture,
                                                 double expirationTime,
                                                 double strike,
                                                 boolean isCall) {
    super(underlyingFuture, expirationTime, strike, isCall);
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

}
