/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link Trade} to the appropriate {@link InstrumentDefinition}
 */
public class TradeConverter {
  /** Converter for futures excluding Federal funds futures */
  private final FutureTradeConverter _futureTradeConverter;
  /** Converter for Federal funds futures */
  private final FederalFundsFutureTradeConverter _federalFundsFutureTradeConverter;
  /** Converter for all other securities */
  private final FinancialSecurityVisitor<InstrumentDefinition<?>> _securityConverter;

  /**
   * Note that this constructor explicitly sets the Federal funds future converter to null.
   * @param futureTradeConverter The futures trade converter, not null
   * @param securityConverter The future security converter, not null
   */
  public TradeConverter(final FutureTradeConverter futureTradeConverter, final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter) {
    ArgumentChecker.notNull(futureTradeConverter, "future trade converter");
    ArgumentChecker.notNull(securityConverter, "security converter");
    _futureTradeConverter = futureTradeConverter;
    _federalFundsFutureTradeConverter = null;
    _securityConverter = securityConverter;
  }

  /**
   * @param futureTradeConverter The futures trade converter, not null
   * @param federalFundsFutureTradeConverter The Federal funds future trade converter, not null
   * @param securityConverter The security converter, not null
   */
  public TradeConverter(final FutureTradeConverter futureTradeConverter, final FederalFundsFutureTradeConverter federalFundsFutureTradeConverter,
      final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter) {
    ArgumentChecker.notNull(futureTradeConverter, "future trade converter");
    ArgumentChecker.notNull(federalFundsFutureTradeConverter, "Federal funds future trade converter");
    ArgumentChecker.notNull(securityConverter, "security converter");
    _futureTradeConverter = futureTradeConverter;
    _federalFundsFutureTradeConverter = federalFundsFutureTradeConverter;
    _securityConverter = securityConverter;
  }

  /**
   * Converts a {@link Trade} to a {@link InstrumentDefinition}
   * @param trade The trade, not null
   * @return The instrument definition
   * @throws IllegalArgumentException if the underlying security is not a {@link FinancialSecurity}
   */
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    ArgumentChecker.isTrue(security instanceof FinancialSecurity, "Security must be a FinancialSecurity");
    if (security instanceof FederalFundsFutureSecurity) {
      return _federalFundsFutureTradeConverter.convert(trade);
    }
    if (security instanceof FutureSecurity) {
      return _futureTradeConverter.convert(trade);
    }
    return ((FinancialSecurity) security).accept(_securityConverter);
  }
}
