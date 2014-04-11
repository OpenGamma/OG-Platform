/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.DaysFromEndOfMonthExpiryAdjuster;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides ExternalIds for equity future options used to build a volatility surface
 */
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider extends BloombergFutureOptionVolatilitySurfaceInstrumentProvider {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider.class);
  /** The date-time formatter */
  private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
  /** The expiry rules */
  private static final HashMap<String, ExchangeTradedInstrumentExpiryCalculator> EXPIRY_RULES;
  /** An empty holiday calendar */
  private static final Calendar NO_HOLIDAYS = new NoHolidayCalendar();
  static {
    EXPIRY_RULES = new HashMap<>();
    EXPIRY_RULES.put("NKY", FutureOptionExpiries.of(new NextExpiryAdjuster(2, DayOfWeek.FRIDAY)));
    EXPIRY_RULES.put("NDX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1))); //TODO
    EXPIRY_RULES.put("RUT", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1))); //TODO
    EXPIRY_RULES.put("DJX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    EXPIRY_RULES.put("SPX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    EXPIRY_RULES.put("VIX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, -30)));
    EXPIRY_RULES.put("AAPL US", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    EXPIRY_RULES.put("UKX", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY)));
    EXPIRY_RULES.put("FB US", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    EXPIRY_RULES.put("TWSE", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.WEDNESDAY)));
    EXPIRY_RULES.put("HSCEI", new DaysFromEndOfMonthExpiryAdjuster(1));
    EXPIRY_RULES.put("RDXUSD", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.THURSDAY, 1)));
    EXPIRY_RULES.put("DEFAULT", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    //TODO DAX, EUROSTOXX 50 (SX5E)
  }

  /**
   * Uses the default ticker scheme (BLOOMBERG_TICKER_WEAK)
   * @param futureOptionPrefix the prefix to the resulting code (e.g. DJX), not null
   * @param postfix the postfix to the resulting code (e.g. Index), not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the exchange id, not null
   */
  public BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName);
  }

  /**
   * @param futureOptionPrefix the prefix to the resulting code (e.g. DJX), not null
   * @param postfix the postfix to the resulting code (e.g. Index), not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the exchange id, not null
   * @param schemeName the ticker scheme name, not null
   */
  public BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName, final String schemeName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName, schemeName);
  }

  /**
   * Provides an ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + date + callPutFlag + strike + postfix <p>
   * e.g. DJX 12/21/13 C145.0 Index
   * <p>
   * @param futureOptionNumber n'th future following curve date, not null
   * @param strike option's strike, expressed as price in %, e.g. 98.750, not null
   * @param surfaceDate date of curve validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    ArgumentChecker.notNull(futureOptionNumber, "futureOptionNumber");
    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.notNull(surfaceDate, "surface date");
    final String prefix = getFutureOptionPrefix();
    final StringBuffer ticker = new StringBuffer();
    ticker.append(prefix);
    ticker.append(" ");
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = getExpiryRuleCalculator();
    final LocalDate expiry = expiryRule.getExpiryDate(futureOptionNumber.intValue(), surfaceDate, NO_HOLIDAYS);
    ticker.append(FORMAT.format(expiry));
    ticker.append(" ");
    ticker.append(strike > useCallAboveStrike() ? "C" : "P");
    ticker.append(strike);
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(getScheme(), ticker.toString());
  }

  /**
   * Gets the expiryRules.
   * @return the expiryRules
   */
  public static HashMap<String, ExchangeTradedInstrumentExpiryCalculator> getExpiryRules() {
    return EXPIRY_RULES;
  }

  @Override
  public ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator() {
    final String prefix = getFutureOptionPrefix();
    ExchangeTradedInstrumentExpiryCalculator expiryRule = EXPIRY_RULES.get(prefix);
    if (expiryRule == null) {
      s_logger.info("No expiry rule has been setup for " + prefix + ". Using Default of 3rd Friday.");
      expiryRule = EXPIRY_RULES.get("DEFAULT");
    }
    return expiryRule;
  }
}
