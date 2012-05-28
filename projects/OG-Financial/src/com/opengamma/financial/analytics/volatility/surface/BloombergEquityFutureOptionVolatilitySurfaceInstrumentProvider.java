/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.equity.NextEquityExpiryAdjuster;
import com.opengamma.financial.analytics.model.equity.SaturdayAfterThirdFridayAdjuster;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Provides ExternalId's for EquityFutureOptions used to build the Volatility Surface  
 */
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider extends BloombergFutureOptionVolatilitySurfaceInstrumentProvider {

  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
  private static final DateTimeFormatter FORMAT = DateTimeFormatters.pattern("MM/dd/yy");
  private static final FutureOptionExpiries EXPIRY_UTILS = FutureOptionExpiries.of(new SaturdayAfterThirdFridayAdjuster(), new NextEquityExpiryAdjuster()); 
  
  /**
   * @param futureOptionPrefix the prefix to the resulting code (eg DJX)
   * @param postfix the postfix to the resulting code (eg Index)
   * @param dataFieldName the name of the data field. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts
   */
  public BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(String futureOptionPrefix, String postfix, String dataFieldName, Double useCallAboveStrike) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike);
  }

  @Override
  /**
   * Provides ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + date + callPutFlag + strike + postfix <p>
   * eg DJX 12/21/13 C145.0 Index
   * <p>
   * @param futureNumber n'th future following curve date
   * @param strike option's strike, expressed as price in %, e.g. 98.750
   * @param surfaceDate date of curve validity; valuation date
   */
  public ExternalId getInstrument(Number futureOptionNumber, Double strike, LocalDate surfaceDate) {
    Validate.notNull(futureOptionNumber, "futureOptionNumber");
    final StringBuffer ticker = new StringBuffer();
    ticker.append(getFutureOptionPrefix());
    ticker.append(" ");
    LocalDate expiry = EXPIRY_UTILS.getFutureOptionExpiry(futureOptionNumber.intValue(), surfaceDate);
    ticker.append(FORMAT.print(expiry));
    ticker.append(" ");
    ticker.append(strike > useCallAboveStrike() ? "C" : "P");
    ticker.append(strike);
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(SCHEME, ticker.toString());
  }

}
