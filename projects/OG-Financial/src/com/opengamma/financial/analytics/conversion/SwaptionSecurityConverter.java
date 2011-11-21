/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class SwaptionSecurityConverter implements SwaptionSecurityVisitor<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;
  @SuppressWarnings("unused")
  private final ConventionBundleSource _conventionSource;
  private final SwapSecurityConverter _swapConverter;

  public SwaptionSecurityConverter(final SecuritySource securitySource, final ConventionBundleSource conventionSource, final SwapSecurityConverter swapConverter) {
    Validate.notNull(securitySource, "security source");
    Validate.notNull(swapConverter, "swap converter");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _swapConverter = swapConverter;
  }

  @Override
  public InstrumentDefinition<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
    Validate.notNull(swaptionSecurity, "swaption security");
    final ExternalId underlyingIdentifier = swaptionSecurity.getUnderlyingId();
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();
    final InstrumentDefinition<?> underlyingSwap = ((SwapSecurity) _securitySource.getSecurity(ExternalIdBundle.of(underlyingIdentifier))).accept(_swapConverter);
    if (!(underlyingSwap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Need a fixed-float swap to create a swaption");
    }
    final SwapFixedIborDefinition fixedFloat = (SwapFixedIborDefinition) underlyingSwap;
    final boolean isCashSettled = swaptionSecurity.isCashSettled();
    final boolean isLong = swaptionSecurity.isLong();
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, fixedFloat, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, fixedFloat, isLong);
  }
}
