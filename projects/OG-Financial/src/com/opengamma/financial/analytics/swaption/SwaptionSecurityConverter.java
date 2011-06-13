/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swaption;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class SwaptionSecurityConverter implements SwaptionSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final SwapSecurityConverter _swapConverter;

  public SwaptionSecurityConverter(final SwapSecurityConverter swapConverter) {
    Validate.notNull(swapConverter, "swap converter");
    _swapConverter = swapConverter;
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
    Validate.notNull(swaptionSecurity, "swaption security");
    final boolean isCashSettled = swaptionSecurity.getIsCashSettled();
    final boolean isLong = swaptionSecurity.getIsLong();
    //Identifier underlyingIdentifier = swaptionSecurity.getUnderlyingIdentifier();
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();
    final SwapSecurity swapSecurity = null; //TODO how do I get this?
    final FixedIncomeInstrumentConverter<?> swap = _swapConverter.visitSwapSecurity(swapSecurity);
    if (!(swap instanceof SwapFixedIborDefinition)) {
      throw new OpenGammaRuntimeException("Need a fixed-float swap to create a swaption");
    }
    final SwapFixedIborDefinition fixedFloat = (SwapFixedIborDefinition) swap;
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, fixedFloat, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, fixedFloat, isLong);
  }

}
