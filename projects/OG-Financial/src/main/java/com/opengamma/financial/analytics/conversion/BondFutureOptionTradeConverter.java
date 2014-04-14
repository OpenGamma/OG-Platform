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
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionBlackFunction;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BondFutureOptionTradeConverter implements TradeConverter {
  private final BondFutureOptionSecurityConverter _securityConverter;

  public BondFutureOptionTradeConverter(final BondFutureOptionSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  //TODO see comments in InterestRateFutureOptionTradeConverter
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondFutureOptionSecurity, "Can only handle trades with security type BondFutureOptionSecurity");
    final InstrumentDefinition<?> securityDefinition = ((BondFutureOptionSecurity) trade.getSecurity()).accept(_securityConverter);
    ArgumentChecker.notNull(trade.getPremium(), "Bond future option trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    //TODO fix the next two lines - it's here to avoid double-multiplying when stuff is scaled at the position level
    final int quantity = 1;
    final double premium = -trade.getPremium() * Math.signum(trade.getQuantity().doubleValue());
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

    final BondFutureOptionPremiumSecurityDefinition underlyingOption = (BondFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new BondFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, premium);
  }
  
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionTradeConverter.class);
}
