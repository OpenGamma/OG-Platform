/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CashSecurityConverter implements CashSecurityVisitor<InstrumentDefinition<?>> {

  @Override
  public CashDefinition visitCashSecurity(final CashSecurity security) {
    Validate.notNull(security, "security");
    final Currency currency = security.getCurrency();
    // TODO: Do we need to adjust the dates to a good business day?
    final ZonedDateTime startDate = security.getStart();
    final ZonedDateTime endDate = security.getMaturity();
    final double accrualFactor = security.getDayCount().getDayCountFraction(startDate, endDate);
    return new CashDefinition(currency, startDate, endDate, security.getAmount(), security.getRate(), accrualFactor);
  }

}
