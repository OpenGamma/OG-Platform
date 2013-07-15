/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import java.util.regex.Matcher;

import com.opengamma.id.ExternalId;

/**
 * 
 */
public abstract class BloombergTickerParserFutureOption extends BloombergTickerParser {

  /** the symbol */
  private String _symbol;
  /** C or P */
  private String _callOrPut;
  /** expiry */
  private String _expiry;
  /** the strike */
  private String _strike;

  /**
   * Parse given ticker
   * 
   * @param value ticker as string
   */
  public BloombergTickerParserFutureOption(final String value) {
    super(value);
  }

  /**
   * Parse given id (should be a ticker)
   * 
   * @param identifier id to parse
   */
  public BloombergTickerParserFutureOption(final ExternalId identifier) {
    super(identifier);
  }

  /**
   * The symbol
   * @return the symbol 
   */
  public String getSymbol() {
    return _symbol;
  }

  /**
   * is call or put
   * @return "C" or "P"
   */
  public String getCallOrPut() {
    return _callOrPut;
  }

  /**
   * get the expiry
   * @return the expiry
   */
  public String getExpiry() {
    return _expiry;
  }

  /**
   * get the strike
   * @return the strike
   */
  public String getStrike() {
    return _strike;
  }

  @Override
  protected void parse(Matcher matcher) {
    _symbol = matcher.group(1);
    _expiry = matcher.group(2);
    _callOrPut = matcher.group(3);
    _strike = matcher.group(4);
  }

  @Override
  protected String getPatternString() {
    return "([\\w ]{2})(\\w{2})([CP]) (\\d+(\\.\\d+)?) " + getTypeName();
  }

  /**
   * The type
   * @return the type 
   */
  public String getTypeName() {
    return "Comdty";
  }

}
