/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts swaptions from {@link SwaptionSecurity} to the {@link InstrumentDefinition}s.
 */
public class SwaptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;
  private final SwapSecurityConverter _swapConverter;

  /**
   * @param securitySource The security source, not null
   * @param swapConverter The underlying swap converter, not null
   */
  public SwaptionSecurityConverter(final SecuritySource securitySource, final SwapSecurityConverter swapConverter) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(swapConverter, "swap converter");
    _securitySource = securitySource;
    _swapConverter = swapConverter;
  }

  @Override
  public InstrumentDefinition<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
    ArgumentChecker.notNull(swaptionSecurity, "swaption security");
    final ExternalId underlyingIdentifier = swaptionSecurity.getUnderlyingId();
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();
    final InstrumentDefinition<?> underlyingSwap = ((SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier))).accept(_swapConverter);
    final SwapDefinition swapDefinition = (SwapDefinition) underlyingSwap;
    final boolean isCashSettled = swaptionSecurity.isCashSettled();
    final boolean isLong = swaptionSecurity.isLong();
    if (swaptionSecurity.getCurrency().equals(Currency.BRL)) {
      if (!(swapDefinition instanceof SwapFixedCompoundedONCompoundedDefinition)) {
        throw new OpenGammaRuntimeException("Underlying BRL swap must be fixed compounded / overnight compounded");
      }
      return isCashSettled ? SwaptionCashFixedCompoundedONCompoundingDefinition.from(expiry, (SwapFixedCompoundedONCompoundedDefinition) swapDefinition, isLong) :
        SwaptionPhysicalFixedCompoundedONCompoundedDefinition.from(expiry, (SwapFixedCompoundedONCompoundedDefinition) swapDefinition, isLong);
    }
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Underlying swap of a swaption must be a fixed / ibor swap");
    }
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Underlying swap of a swaption must be a fixed / ibor swap");
    }
    final SwapFixedIborDefinition fixedIbor = (SwapFixedIborDefinition) swapDefinition;
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, fixedIbor, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, fixedIbor, isLong);
  }
}
