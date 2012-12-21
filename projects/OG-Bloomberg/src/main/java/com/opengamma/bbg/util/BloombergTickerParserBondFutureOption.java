/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;


import com.opengamma.id.ExternalId;

/**
 * <p>
 * Parser for Bloomberg index equity option tickers.  The rationale for having this parsing is to be able to extract instrument
 * indicatives from a Bloomberg ticker string or {@link com.opengamma.id.ExternalId}, without looking up the instrument via
 * Bloomberg security field loading.  This allows the caller to avoid expending Bloomberg security field lookup quota.
 * </p>
 * A legal Bloomberg equity ticker looks like this:  <code>FVX3 P17.5 Comdty</code>.  See the Bloomberg documentation for more details.
 * <p>
 * All dates are represented as {@link javax.time.calendar.LocalDate}. For greater accuracy, users should perform a Bloomberg security lookup.
 * </p>
 * <p>
 * The idiom for using this class is to
 * create a parser instance around the Bloomberg ticker, and then call various getters on the instance to read the indicatives.
 * </p>
 */
public class BloombergTickerParserBondFutureOption extends BloombergTickerParserFutureOption {
  
  /**
   * Create a parser
   * @param ticker a legal Bloomberg ticker, as string.  
   */
  public BloombergTickerParserBondFutureOption(String ticker) {
    super(ticker);
  }
  
  /**
   * Create a parser
   * @param identifier a legal Bloomberg ticker, with {@link com.opengamma.id.ExternalScheme} 
   * of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}.  
   */
  public BloombergTickerParserBondFutureOption(ExternalId identifier) {
    super(identifier);
  }
}
