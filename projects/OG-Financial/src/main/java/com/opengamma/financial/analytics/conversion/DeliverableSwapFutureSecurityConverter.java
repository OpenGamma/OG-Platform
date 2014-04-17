/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DeliverableSwapFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** The swap converter */
  private final SwapSecurityConverter _swapConverter;

  public DeliverableSwapFutureSecurityConverter(final SecuritySource securitySource, final SwapSecurityConverter swapConverter) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(swapConverter, "swap converter");
    _securitySource = securitySource;
    _swapConverter = swapConverter;
  }

  @Override
  public SwapFuturesPriceDeliverableSecurityDefinition visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId swapIdentifer = security.getUnderlyingSwapId();
    final SwapSecurity underlyingSwapSecurity = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(swapIdentifer)); //TODO see note in InterestRateFutureOptionSecurityConverter
    if (underlyingSwapSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying swap security " + swapIdentifer + " was not found in database");
    }
    final InstrumentDefinition<?> underlyingSwap = underlyingSwapSecurity.accept(_swapConverter);
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Underlying swap was not fixed / ibor float");
    }
    final ZonedDateTime lastTradingDate = security.getExpiry().getExpiry();
    final double notional = security.getNotional();
    return new SwapFuturesPriceDeliverableSecurityDefinition(lastTradingDate, (SwapFixedIborDefinition) underlyingSwap, notional);
  }
  
}
