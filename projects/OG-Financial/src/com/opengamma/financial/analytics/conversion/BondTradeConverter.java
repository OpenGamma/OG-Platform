/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class BondTradeConverter {
  private final BondSecurityConverter _securityConverter;

  public BondTradeConverter(final BondSecurityConverter securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public BondFixedTransactionDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof BondSecurity, "Can only handle trades with security type BondSecurity");
    final BondSecurity security = (BondSecurity) trade.getSecurity();
    final InstrumentDefinition<?> underlying = security.accept(_securityConverter);
    if (!(underlying instanceof BondFixedSecurityDefinition)) {
      throw new OpenGammaRuntimeException("Can only handle fixed coupon bonds");
    }
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) underlying;
    final int quantity = trade.getQuantity().intValue();
    final ZonedDateTime settlementDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO
    final double price = trade.getPremium().doubleValue();
    return new BondFixedTransactionDefinition(bond, quantity, settlementDate, price);
  }
}
