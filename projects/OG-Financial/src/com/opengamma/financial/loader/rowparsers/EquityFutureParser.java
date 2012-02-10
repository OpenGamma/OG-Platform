/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparsers;

import java.math.BigDecimal;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Parses an equity future trade from a row's data.
 * TODO this shouldn't really be instantiable, as no object state is maintained
 */
public class EquityFutureParser extends RowParser {

  private static final String ID_SCHEME = "MANUAL_LOAD";

  //CSOFF
  protected String EXPIRY = "expiry";
  protected String SETTLEMENT_DATE = "settlement date";
  protected String TRADING_EXCHANGE = "trading exchange";
  protected String SETTLEMENT_EXCHANGE = "settlement exchange";
  protected String CURRENCY = "currency";
  protected String UNIT_AMOUNT = "unit amount";
  protected String UNDERLYING_ID = "underlying id";
  protected String NAME = "name";
  protected String BBG_CODE = "bbg code";
  protected String NUMBER_OF_CONTRACTS = "number of contracts";
  protected String TRADE_DATE = "trade date";
  protected String REFERENCE_PRICE = "reference price";
  //CSON

  public EquityFutureParser(LoaderContext loaderContext) {
    super(loaderContext);
  }

 /**
   * Creates a Trade from a security, a position and details provided from file
   * @param eqFutureDetails The parsed values of the input file
   * @param security The security
   * @param position The position
   * @return the newly constructed trade
   */
  @Override
  public ManageableTrade constructTrade(Map<String, String> eqFutureDetails, ManageableSecurity security, ManageablePosition position) {

    final LocalDate tradeDate = getDateWithException(eqFutureDetails, TRADE_DATE);
    ExternalId ct = ExternalId.of("ID", "COUNTERPARTY"); // TODO: Hardcoded COUNTERPARTY
    final BigDecimal nContracts = new BigDecimal(Double.parseDouble(getWithException(eqFutureDetails, NUMBER_OF_CONTRACTS)));

    ManageableTrade trade = new ManageableTrade(nContracts, security.getExternalIdBundle(), tradeDate, OffsetTime.of(13, 30, 0, ZoneOffset.UTC), ct);
    trade.setSecurityLink(ManageableSecurityLink.of(security));

    // TODO ELAINE/CASE! Overloaded trade._premium as a reference price!?
    final Double referencePrice = Double.parseDouble(getWithException(eqFutureDetails, REFERENCE_PRICE));
    trade.setPremium(referencePrice);

    return trade;
  }

  /**
   * Creates a Security from details provided from file
   * @param eqFutureDetails The parsed values of the input file
   * @return the newly constructed security
   */
  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> eqFutureDetails) {

    final Currency ccy = Currency.of(getWithException(eqFutureDetails, CURRENCY));
    final String tradingExchange = getWithException(eqFutureDetails, TRADING_EXCHANGE);
    final String settlementExchange = getWithException(eqFutureDetails, SETTLEMENT_EXCHANGE);
    final double unitAmount = Double.parseDouble(getWithException(eqFutureDetails, UNIT_AMOUNT));
    final String bbgId = getWithException(eqFutureDetails, UNDERLYING_ID);
    final ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);

    // TODO: Make Date/Time/Zone treatment consistent : As it stands, the portfolio definition files only specify dates as dd/MM/yyyy. 
    // Information of time and Zone, as read by various methods is stubbed in.
    final LocalDate expiryDate = getDateWithException(eqFutureDetails, EXPIRY);
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(expiryDate, LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    final ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.of(getDateWithException(eqFutureDetails, SETTLEMENT_DATE), LocalTime.of(16, 0)), TimeZone.UTC);

    final EquityFutureSecurity security = new EquityFutureSecurity(expiry, tradingExchange, settlementExchange, ccy, unitAmount, settlementDate, underlyingID);
    final String identifierValue = getWithException(eqFutureDetails, BBG_CODE);
    security.addExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue));
    final String name = getWithException(eqFutureDetails, NAME);
    security.setName(name);
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    
    ManageableSecurity[] result = {security};
    return result;
  }

}
