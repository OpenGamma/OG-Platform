/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 *
 */
public class BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider extends BloombergFutureOptionVolatilitySurfaceInstrumentProvider {
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
  private static final DecimalFormat FORMATTER = new DecimalFormat("###.##");
  static {
    FORMATTER.setMinimumFractionDigits(2);
  }

  /**
   * @param futureOptionPrefix the prefix to the resulting code
   * @param postfix the postfix to the resulting code
   * @param dataFieldName the name of the data field. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts
   * @param exchangeIdName the id of the exchange
   */
  public BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName);
  }

  /**
   * Provides ExternalID for Bloomberg ticker, eg RXZ3C 100 Comdty,
   * given a reference date and an integer offset, the n'th subsequent option
   * The format is futurePrefix + month + year + callPutFlag + strike + postfix
   *
   * @param futureOptionNumber n'th future following curve date
   * @param strike option's strike, expressed as price, e.g. 100
   * @param surfaceDate date of curve validity; valuation date
   * @return The external id for the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    Validate.notNull(futureOptionNumber, "futureOptionNumber");
    final StringBuffer ticker = new StringBuffer();
    ticker.append(getFutureOptionPrefix());
    ticker.append(BloombergFutureUtils.getExpiryCodeForBondFutureOptions(getFutureOptionPrefix(), futureOptionNumber.intValue(), surfaceDate));
    ticker.append(strike > useCallAboveStrike() ? "C " : "P ");
    ticker.append(FORMATTER.format(strike));
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(SCHEME, ticker.toString());
  }
}
