/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import java.util.regex.Matcher;

import com.opengamma.id.ExternalId;

/**
 * <p>
 * Parser for Bloomberg cash equity tickers.  The rationale for having this parsing is to be able to extract instrument
 * indicatives from a Bloomberg ticker string or {@link com.opengamma.id.ExternalId}, without looking up the instrument via
 * Bloomberg security field loading.  This allows the caller to avoid expending Bloomberg security field lookup quota.
 * </p>
 * A legal Bloomberg equity ticker looks like this:  <code>MSFT US Equity</code>.  See the Bloomberg documentation for more details.
 * <p>
 * The idiom for using this class is to
 * create a parser instance around the Bloomberg ticker, and then call various getters on the instance to read the indicatives.
 * </p>
 * @author noah@opengamma
 */
public class BloombergTickerParserEQ extends BloombergTickerParser {
  // ------------ FIELDS ------------
  private String _symbol;
  private String _exchangeCode;
  
  
  
  // ------------ METHODS ------------
  // -------- CONSTRUCTORS --------
  /**
   * Create a parser
   * @param ticker a legal Bloomberg ticker, as string.  
   * A legal Bloomberg equity ticker looks like this:  <code>MSFT US Equity</code>.  See the Bloomberg documentation for more details.
   */
  public BloombergTickerParserEQ(String ticker) {
    super(ticker);
  }

  /**
   * Create a parser
   * @param identifier a legal Bloomberg ticker, with {@link com.opengamma.id.ExternalScheme} 
   * of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}.  A legal Bloomberg equity 
   * ticker looks like this:  <code>MSFT US Equity</code>.  See the Bloomberg documentation for more details.
   */
  public BloombergTickerParserEQ(ExternalId identifier) {
    super(identifier);
  }

  
  // -------- ABSTRACT IMPLEMENTATIONS --------  
  /**
   * Do not call
   * @return regex for this implementation
   */
  @Override
  protected String getPatternString() {
    return "(\\w+) (\\w+) Equity";
  }

  /**
   * Do not call
   * @param matcher 
   */
  @Override
  protected void parse(Matcher matcher) {
    _symbol = matcher.group(1);
    _exchangeCode = matcher.group(2);
  }
  
  
  // -------- PROPERTIES --------
  /**
   * Return the equity's symbol (e.g. {@code MSFT})
   * @return the equity's symbol 
   */
  public String getSymbol() {
    return _symbol;
  }
  
  /**
   * Return the equity's exchange code (e.g. {@code US})
   * @return the equity's exchange code 
   */
  public String getExchangeCode() {
    return _exchangeCode;
  }
}
