/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;

/**
 * FIXME CASE - BondFutureDefinition needs a reference price. Without a trade, where will it come from?
 */
public class BondFutureSecurityConverter extends AbstractFutureSecurityVisitor<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;
  private final BondSecurityConverter _bondConverter;

  public BondFutureSecurityConverter(final SecuritySource securitySource, final BondSecurityConverter bondConverter) {
    Validate.notNull(securitySource, "security source");
    Validate.notNull(bondConverter, "bond converter");
    _securitySource = securitySource;
    _bondConverter = bondConverter;
  }

  @Override
  public InstrumentDefinition<?> visitBondFutureSecurity(final BondFutureSecurity bondFuture) {
    Validate.notNull(bondFuture);
    final ZonedDateTime tradingLastDate = bondFuture.getExpiry().getExpiry();
    final ZonedDateTime noticeFirstDate = bondFuture.getFirstDeliveryDate();
    final ZonedDateTime noticeLastDate = bondFuture.getLastDeliveryDate();
    final double notional = bondFuture.getUnitAmount();
    final List<BondFutureDeliverable> basket = bondFuture.getBasket();
    final int n = basket.size();
    final BondFixedSecurityDefinition[] deliverables = new BondFixedSecurityDefinition[n];
    final double[] conversionFactor = new double[n];
    for (int i = 0; i < n; i++) {
      final BondFutureDeliverable deliverable = basket.get(i);
      final BondSecurity bondSecurity = (BondSecurity) _securitySource.getSecurity(deliverable.getIdentifiers());
      deliverables[i] = (BondFixedSecurityDefinition) bondSecurity.accept(_bondConverter); //TODO check type
      conversionFactor[i] = deliverable.getConversionFactor();
    }
    return new BondFutureDefinition(tradingLastDate, noticeFirstDate, noticeLastDate, notional, deliverables, conversionFactor);
  }
}
