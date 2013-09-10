/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts swaptions from {@link SwaptionSecurity} to the {@link InstrumentDefinition}s.
 * @deprecated Replaced by {@link SwaptionSecurityConverter}, which does not use curve name information
 */
@Deprecated
public class SwaptionSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;
  private final SwapSecurityConverterDeprecated _swapConverter;

  /**
   * @param securitySource The security source, not null
   * @param swapConverter The underlying swap converter, not null
   */
  public SwaptionSecurityConverterDeprecated(final SecuritySource securitySource, final SwapSecurityConverterDeprecated swapConverter) {
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
    final SwapDefinition fixedFloat = (SwapDefinition) underlyingSwap;
    final boolean isCashSettled = swaptionSecurity.isCashSettled();
    final boolean isLong = swaptionSecurity.isLong();
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, fixedFloat, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, fixedFloat, isLong);
  }
}
