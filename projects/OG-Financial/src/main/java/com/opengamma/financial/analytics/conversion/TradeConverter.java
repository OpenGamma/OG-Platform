/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link Trade} to the appropriate {@link InstrumentDefinition}
 */
public class TradeConverter {
  /** Converter for futures */
  private final FutureTradeConverter _futureTradeConverter;
  /** Converter for all other securities */
  private final FinancialSecurityVisitor<InstrumentDefinition<?>> _securityConverter;

  public TradeConverter(final FutureTradeConverter futureTradeConverter, final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter) {
    ArgumentChecker.notNull(futureTradeConverter, "future trade converter");
    ArgumentChecker.notNull(securityConverter, "security converter");
    _futureTradeConverter = futureTradeConverter;
    _securityConverter = securityConverter;
  }

  /**
   * Converts a {@link Trade} to a {@link InstrumentDefinition}
   * @param trade The trade, not null
   * @return The instrument definition
   * @throw IllegalArgumentException if the underlying security is not a {@link FinancialSecurity}
   */
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof FinancialSecurity, "Security must be a FinancialSecurity");
    if (trade.getSecurity() instanceof FutureSecurity) {
      return _futureTradeConverter.convert(trade);
    }
    return ((FinancialSecurity) trade.getSecurity()).accept(_securityConverter);
  }
}
