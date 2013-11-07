/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.expirycalc.BondFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider extends BloombergFutureOptionVolatilitySurfaceInstrumentProvider {
  private static final DecimalFormat FORMATTER = new DecimalFormat("###.##");
  static {
    FORMATTER.setMinimumFractionDigits(2);
  }

  /**
   * Uses the default ticker scheme (BLOOMBERG_TICKER_WEAK)
   * @param futureOptionPrefix the prefix to the resulting code, not null
   * @param postfix the postfix to the resulting code, not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the id of the exchange, not null
   */
  public BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName);
  }

  /**
   * @param futureOptionPrefix the prefix to the resulting code, not null
   * @param postfix the postfix to the resulting code, not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the id of the exchange, not null
   * @param schemeName the name of the ticker scheme, not null
   */
  public BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName, final String schemeName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName, schemeName);
  }

  /**
   * Provides ExternalID for Bloomberg ticker, e.g. RXZ3C 100 Comdty,
   * given a reference date and an integer offset, the n'th subsequent option
   * The format is futurePrefix + month + year + callPutFlag + strike + postfix
   *
   * @param futureOptionNumber n'th future following curve date, not null
   * @param strike option's strike, expressed as price, e.g. 100, not null
   * @param surfaceDate date of curve validity; valuation date, not null
   * @return The external id for the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    ArgumentChecker.notNull(futureOptionNumber, "futureOptionNumber");
    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.notNull(surfaceDate, "surface date");
    final StringBuffer ticker = new StringBuffer();
    ticker.append(getFutureOptionPrefix());
    ticker.append(BloombergFutureUtils.getExpiryCodeForBondFutureOptions(getFutureOptionPrefix(), futureOptionNumber.intValue(), surfaceDate));
    ticker.append(strike > useCallAboveStrike() ? "C " : "P ");
    ticker.append(FORMATTER.format(strike));
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(getScheme(), ticker.toString());
  }

  @Override
  public ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator() {
    return BondFutureOptionExpiryCalculator.getInstance();
  }
}
