/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts bond trades into {@link BondFixedTransactionDefinition}s.
 */
public class BondTradeConverter implements TradeConverter {
  /** Converts the underlying security */
  private final BondSecurityConverter _securityConverter;

  /**
   * @param securityConverter The security converter, not null
   */
  public BondTradeConverter(final BondSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  /**
   * Converts a fixed coupon bond trade into a {@link BondFixedTransactionDefinition}.
   * @param trade The trade, not null. Must be a {@link BondSecurity}
   * @return The transaction definition
   */
  public BondFixedTransactionDefinition convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondSecurity, "Can only handle trades with security type BondSecurity");
    final LocalDate tradeDate = trade.getTradeDate();
    if (tradeDate == null) {
      throw new OpenGammaRuntimeException("Trade date should not be null");
    }
    if (trade.getTradeTime() == null) {
      throw new OpenGammaRuntimeException("Trade time should not be null");
    }
    if (trade.getPremium() == null) {
      throw new OpenGammaRuntimeException("Trade premium should not be null.");
    }
    final BondSecurity security = (BondSecurity) trade.getSecurity();
    final InstrumentDefinition<?> underlying = security.accept(_securityConverter);
    if (!(underlying instanceof BondFixedSecurityDefinition)) {
      throw new OpenGammaRuntimeException("Can only handle fixed coupon bonds");
    }
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) underlying;
    final int quantity = trade.getQuantity().intValue(); // MH - 9-May-2013: changed from 1. // REVIEW: The quantity mechanism should be reviewed.
    final ZonedDateTime settlementDate = trade.getTradeDate().atTime(trade.getTradeTime()).atZoneSameInstant(ZoneOffset.UTC); //TODO get the real time zone
    final double price = trade.getPremium().doubleValue();
    return new BondFixedTransactionDefinition(bond, quantity, settlementDate, price);
  }

}
