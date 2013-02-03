/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.simpleinstruments.definition.SimpleFXFutureDefinition;
import com.opengamma.analytics.financial.simpleinstruments.definition.SimpleInstrumentDefinition;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.FXFutureSecurity;

/**
 *
 */
public class SimpleFutureConverter extends FinancialSecurityVisitorAdapter<SimpleInstrumentDefinition<?>> {

  @Override
  public SimpleInstrumentDefinition<?> visitFXFutureSecurity(final FXFutureSecurity security) {
    Validate.notNull(security, "security");
    final ZonedDateTime expiry = security.getExpiry().getExpiry();
    final double referencePrice = 0;
    return new SimpleFXFutureDefinition(expiry, expiry, referencePrice, security.getNumerator(), security.getDenominator(), security.getUnitAmount());
  }

}
