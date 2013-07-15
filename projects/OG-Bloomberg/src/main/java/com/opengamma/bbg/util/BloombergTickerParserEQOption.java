/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import java.util.regex.Matcher;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;

/**
 * <p>
 * Parser for Bloomberg equity option tickers.  The rationale for having this parsing is to be able to extract instrument
 * indicatives from a Bloomberg ticker string or {@link com.opengamma.id.ExternalId}, without looking up the instrument via
 * Bloomberg security field loading.  This allows the caller to avoid expending Bloomberg security field lookup quota.
 * </p>
 * A legal Bloomberg equity ticker looks like this:  <code>SPX US 01/21/12 P17.5 {Type}</code>.  See the Bloomberg documentation for more details.
 * <p>
 * All dates are represented as {@link javax.time.calendar.LocalDate}. For greater accuracy, users should perform a Bloomberg security lookup.
 * </p>
 * <p>
 * The idiom for using this class is to
 * create a parser instance around the Bloomberg ticker, and then call various getters on the instance to read the indicatives.
 * </p>
 * @author noah@opengamma
 */
public abstract class BloombergTickerParserEQOption extends BloombergTickerParser {
  

  public static BloombergTickerParserEQOption getOptionParser(ExternalId optionTicker) {
    //TODO this is horrid
    if (optionTicker.getValue().endsWith("Equity")) {
      return new BloombergTickerParserEQVanillaOption(optionTicker);
    } else if (optionTicker.getValue().endsWith("Index")) {
      return new BloombergTickerParserEQIndexOption(optionTicker);
    } else {
      throw new OpenGammaRuntimeException("Unknown option type " + optionTicker);
    }
  }
  
  
  // ------------ FIELDS ------------
  private String _symbol;
  private String _exchangeCode;
  private LocalDate _expiry;
  private OptionType _optionType;
  private double _strike;

  // ------------ METHODS ------------
  // -------- CONSTRUCTORS --------
  /**
   * Create a parser
   * @param ticker a legal Bloomberg ticker, as string.  
   */
  public BloombergTickerParserEQOption(String ticker) {
    super(ticker);
  }
  
  /**
   * Create a parser
   * @param identifier a legal Bloomberg ticker, with {@link com.opengamma.id.ExternalScheme} 
   * of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}.  
   */
  public BloombergTickerParserEQOption(ExternalId identifier) {
    super(identifier);
  }

  
  // -------- ABSTRACT IMPLEMENTATIONS --------
  /**
   * Do not call
   * @return regex for this implementation
   */
  @Override
  protected String getPatternString() {
    return "(\\w+) (\\w+) (\\d\\d/\\d\\d/\\d\\d) ([CP])(\\d+(\\.\\d+)?) " + getTypeName();
  }

  /**
   * 
   * @return The type of the underlying for these options, e.g. 'Equity' 'Index'
   */
  protected abstract String getTypeName();

  /**
   * Do not call.
   * @param matcher  the regex matcher, not null
   */
  @Override
  protected void parse(Matcher matcher) {  
    _symbol = matcher.group(1);
    _exchangeCode = matcher.group(2);
    _expiry = LocalDate.parse(matcher.group(3), DateTimeFormatter.ofPattern("MM/dd/yy"));
    _optionType = determineOptionType(matcher.group(4));
    _strike = Double.parseDouble(matcher.group(5));
  }
  
  
  // -------- PROPERTIES --------
  /**
   * Return the underlyer's symbol (e.g. {@code MSFT})
   * @return the underlyer's symbol 
   */
  public String getSymbol() {
    return _symbol;
  }
  
  /**
   * Return the option's exchange code (e.g. {@code US})
   * @return the option's exchange code
   */
  public String getExchangeCode() {
    return _exchangeCode;
  }

  /**
   * Return the option's expiry
   * @return the option's expiry
   */
  public LocalDate getExpiry() {
    return _expiry;
  }

  /**
   * Return the option's type ({@link com.opengamma.financial.security.option.OptionType#CALL CALL}
   * or {@link com.opengamma.financial.security.option.OptionType#PUT PUT})
   * @return the option's type
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * Return the option's strike
   * @return the option's strike
   */
  public double getStrike() {
    return _strike;
  }
  
  
  
  @Override
  public String toString() {
    return "BloombergTickerParserEQOption [symbol=" + _symbol + ", exchangeCode=" + _exchangeCode + ", expiry="
        + _expiry + ", optionType=" + _optionType + ", strike=" + _strike + "]";
  }

  // -------- PRIVATE SUBROUTINES --------
  private OptionType determineOptionType(String optionTypeCode) {
    OptionType optionType = null;
    optionTypeCode = optionTypeCode.toUpperCase();
    if (optionTypeCode.equals("C")) {
      optionType = OptionType.CALL;
    } else if (optionTypeCode.equals("P")) {
      optionType = OptionType.PUT;
    } else {
      throw new OpenGammaRuntimeException("Invalid option type code: " + optionTypeCode);
    }
    return optionType;
  }
}
