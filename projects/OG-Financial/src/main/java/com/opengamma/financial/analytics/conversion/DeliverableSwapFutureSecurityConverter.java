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
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Converter for a deliverable swap future.
 */
public class DeliverableSwapFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** The swap converter */
  private final SwapSecurityConverter _swapConverter;
  /** The interest rate swap converter */
  private final InterestRateSwapSecurityConverter _irSwapConverter;

  /**
   * Constructs a converter for deliverable swap futures.
   * @param securitySource the security source used to load the underlying swap.
   * @param swapConverter the swap converter, only used if the underlying is a {@link SwapSecurity}.
   * @param irSwapConverter the swap converter, only used if the underlying is a {@link InterestRateSwapSecurity}.
   */
  public DeliverableSwapFutureSecurityConverter(final SecuritySource securitySource,
                                                final SwapSecurityConverter swapConverter,
                                                final InterestRateSwapSecurityConverter irSwapConverter) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(irSwapConverter, "swap converter");
    _securitySource = securitySource;
    _swapConverter = swapConverter;
    _irSwapConverter = irSwapConverter;
  }

  @Override
  public SwapFuturesPriceDeliverableSecurityDefinition visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId swapIdentifer = security.getUnderlyingSwapId();
    final Security underlyingSwapSecurity = _securitySource.getSingle(ExternalIdBundle.of(swapIdentifer));
    final InstrumentDefinition<?> underlyingSwap;
    if (underlyingSwapSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying swap security " + swapIdentifer + " was not found in database");
    } else if (underlyingSwapSecurity instanceof InterestRateSwapSecurity) {
      underlyingSwap = ((InterestRateSwapSecurity) underlyingSwapSecurity).accept(_irSwapConverter);
    } else if (underlyingSwapSecurity instanceof SwapSecurity) {
      underlyingSwap = ((SwapSecurity) underlyingSwapSecurity).accept(_swapConverter);
    } else {
      throw new OpenGammaRuntimeException("Unsupported underlying security " + swapIdentifer);
    }
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Underlying swap was not fixed / ibor float");
    }
    final ZonedDateTime lastTradingDate = security.getExpiry().getExpiry();
    final double notional = security.getNotional();
    return new SwapFuturesPriceDeliverableSecurityDefinition(lastTradingDate, (SwapFixedIborDefinition) underlyingSwap, notional);
  }
  
}
