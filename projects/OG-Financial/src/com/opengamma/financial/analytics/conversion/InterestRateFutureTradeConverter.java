/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * TODO CASE FUTURE REFACTOR
 */
public class InterestRateFutureTradeConverter {
  private final InterestRateFutureSecurityConverter _securityConverter;

  public InterestRateFutureTradeConverter(final InterestRateFutureSecurityConverter securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InterestRateFutureSecurityDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof InterestRateFutureSecurity,
        "Can only handle trades with security type InterestRateFutureSecurity");
    return (InterestRateFutureSecurityDefinition) _securityConverter
        .visitInterestRateFutureSecurity((InterestRateFutureSecurity) trade.getSecurity());
  }
}
