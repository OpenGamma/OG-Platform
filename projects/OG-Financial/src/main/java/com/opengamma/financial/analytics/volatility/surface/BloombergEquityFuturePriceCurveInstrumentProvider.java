/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *  Provider of equity Future Instrument ID's.
 */
public class BloombergEquityFuturePriceCurveInstrumentProvider implements FuturePriceCurveInstrumentProvider<Number> {

  /**
   * Gets the expiryRules.
   * @return the expiryRules
   */
  public static Map<?, ?> getExpiryRules() {
    return EXPIRY_RULES;
  }

  private static final HashMap<String, FutureOptionExpiries> EXPIRY_RULES;
  static {
    //TODO: Need to check whether indexes can be supported
    EXPIRY_RULES = new HashMap<>();
    //    EXPIRY_RULES.put("NKY", FutureOptionExpiries.of(new NextExpiryAdjuster(2, DayOfWeek.THURSDAY)));
    //    EXPIRY_RULES.put("NDX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY))); //TODO
    //    EXPIRY_RULES.put("RUT", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY))); //TODO
    //    EXPIRY_RULES.put("DJX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY)));
    //    EXPIRY_RULES.put("SPX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY)));
    //    EXPIRY_RULES.put("VIX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY, -30))); // check this
    //    EXPIRY_RULES.put("UKX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY)));
    EXPIRY_RULES.put("DEFAULT", FutureOptionExpiries.EQUITY_FUTURE);
    //TODO DAX, EUROSTOXX 50 (SX5E)
  }
  
  private static final Calendar WEEKDAYS = new MondayToFridayCalendar("MTWThF");

  private final String _futurePrefix;
  private final String _postfix;
  private final String _dataFieldName;
  private final String _tickerScheme;
  private final String _exchange;

  /**
   * @param futurePrefix e.g. "AAPL="
   * @param postfix generally, "Equity"
   * @param dataFieldName expecting MarketDataRequirementNames.MARKET_VALUE
   * @param tickerScheme expecting BLOOMBERG_TICKER_WEAK or BLOOMBERG_TICKER
   * @param exchange the exchange code e.g. OC
   */
  public BloombergEquityFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName, final String tickerScheme, final String exchange) {
    Validate.notNull(futurePrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    Validate.notNull(tickerScheme, "tickerScheme was null. Try BLOOMBERG_TICKER_WEAK or BLOOMBERG_TICKER");
    _futurePrefix = futurePrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _tickerScheme = tickerScheme;
    _exchange = exchange;
  }

  /** If a 4th argument is not provided, constructor uses BLOOMBERG_TICKER_WEAK as its ExternalScheme
   * @param futurePrefix e.g. "AAPL="
   * @param postfix generally, "Equity"
   * @param dataFieldName expecting MarketDataRequirementNames.MARKET_VALUE
   * @param exchange e.g. "OC"
   */
  public BloombergEquityFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName, final String exchange) {
    Validate.notNull(futurePrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    _futurePrefix = futurePrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _tickerScheme = "BLOOMBERG_TICKER_WEAK";
    _exchange = exchange;
  }

  @Override
  public ExternalId getInstrument(final Number futureNumber) {
    throw new OpenGammaRuntimeException("Provider needs a curve date to create interest rate future identifier from futureNumber");
  }

  @Override
  /**
   * Provides an ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + postfix <p>
   * e.g. AAPL=G3 OC Equity
   * <p>
   * @param futureOptionNumber n'th future following curve date, not null
   * @param curveDate date of future validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  public ExternalId getInstrument(final Number futureNumber, final LocalDate curveDate) {
    ArgumentChecker.notNull(futureNumber, "futureOptionNumber");
    ArgumentChecker.notNull(curveDate, "curve date");
    final StringBuffer ticker = new StringBuffer();
    ticker.append(getFuturePrefix());
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = getExpiryRuleCalculator();
    final LocalDate expiryDate = expiryRule.getExpiryDate(futureNumber.intValue(), curveDate, WEEKDAYS);
    final String expiryCode = BloombergFutureUtils.getShortExpiryCode(expiryDate);
    ticker.append(expiryCode);
    ticker.append(" ");
    if (getExchange() != null) {
      ticker.append(getExchange());
      ticker.append(" ");
    }
    ticker.append(getPostfix());
    return ExternalId.of(getTickerScheme(), ticker.toString());
  }
  
  @Override
  public ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator() {
    ExchangeTradedInstrumentExpiryCalculator expiryRule = EXPIRY_RULES.get(getFuturePrefix());
    if (expiryRule == null) {
      expiryRule = EXPIRY_RULES.get("DEFAULT");
    }
    return expiryRule;
  }
  

  public String getFuturePrefix() {
    return _futurePrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getTickerScheme() {
    return _tickerScheme;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  /**
   * Gets the exchange.
   * @return the exchange
   */
  public String getExchange() {
    return _exchange;
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode() + getExchange().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergEquityFuturePriceCurveInstrumentProvider)) {
      return false;
    }
    final BloombergEquityFuturePriceCurveInstrumentProvider other = (BloombergEquityFuturePriceCurveInstrumentProvider) obj;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName()) &&
        getExchange().equals(other.getExchange());
  }

}
