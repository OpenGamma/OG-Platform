/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converter used to create a bond future option OG-Analytics representation from an OG-Financial type from a trade. The trade
 * is needed for conversion to retrieve details such as last traded date and quantity that are only held on the trade itself.
 */
public class BondFutureOptionTradeConverter implements TradeConverter {
  
  /**
   * Converter used to convert the bond future option of the trade.
   */
  private final BondFutureOptionSecurityConverter _securityConverter;
  
  /**
   * Constructs a bond future option converter.
   * @param securityConverter the bond future option converter, not null.
   */
  public BondFutureOptionTradeConverter(final BondFutureOptionSecurityConverter securityConverter) {
    _securityConverter = ArgumentChecker.notNull(securityConverter, "security converter");
  }

  @Override
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondFutureOptionSecurity, "Can only handle trades with security type BondFutureOptionSecurity");
    BondFutureOptionSecurity security = (BondFutureOptionSecurity) trade.getSecurity();
    final InstrumentDefinition<?> securityDefinition = security.accept(_securityConverter);
    ArgumentChecker.notNull(trade.getPremium(), "Bond future option trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    final int quantity = trade.getQuantity().intValue();
    final double premium = trade.getPremium();
    // Get DateTime of trade to add to Transaction. Handle case when time isn't available.
    final ZonedDateTime tradeDate;
    LocalDate tradeDateLocal = trade.getTradeDate();
    OffsetTime tradeTime = trade.getTradeTime();
    if (tradeDateLocal == null) {
      throw new OpenGammaRuntimeException("Trade did not contain a tradeDate:" +  trade.getUniqueId());
    } else if (tradeTime == null) {
      s_logger.debug("Trade did not contain a tradeTime. Using noon UTC. " +  trade.getUniqueId()); 
      tradeDate = ZonedDateTime.of(tradeDateLocal, LocalTime.of(12, 0), ZoneId.of("UTC"));
    } else {
      tradeDate = trade.getTradeDate().atTime(trade.getTradeTime()).atZoneSameInstant(ZoneOffset.UTC); //TODO get the real time zone    
    }

    if (security.isMargined()) {
      final BondFuturesOptionMarginSecurityDefinition option = (BondFuturesOptionMarginSecurityDefinition) securityDefinition;
      return new BondFuturesOptionMarginTransactionDefinition(option, quantity, tradeDate, premium);
    }
    final BondFuturesOptionPremiumSecurityDefinition underlyingOption = (BondFuturesOptionPremiumSecurityDefinition) securityDefinition;
    return new BondFuturesOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, premium);
  }
  
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionTradeConverter.class);
}
