/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;


public class BloombergTickerParserEQVanillaOptionTest {
  //-------- BASIC CASES --------
  @Test
  public void testWithTickerIdentifier() {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption (ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "MSFT US 01/21/12 C17.5 Equity"));;
    testImpl (parser, "MSFT", MonthOfYear.JANUARY, 21, 2012, OptionType.CALL, 17.5);
  }
  
  @Test
  public void testWithTickerString() {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption ("MSFT US 01/21/12 C17.5 Equity");
    testImpl (parser, "MSFT", MonthOfYear.JANUARY, 21, 2012, OptionType.CALL, 17.5);
  }
  
  @Test
  public void testOtherTickerPatternBug() {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption ("AAPL US 01/19/13 C135 Equity");
    testImpl (parser, "AAPL", MonthOfYear.JANUARY, 19, 2013, OptionType.CALL, 135);
  }
  
  private void testImpl (BloombergTickerParserEQVanillaOption parser, 
                         String symbol, MonthOfYear month, int day, int year, OptionType optionType, double strike) {
    assertEquals ("US", parser.getExchangeCode());
    assertEquals (symbol, parser.getSymbol());
    
    LocalDate expiry = parser.getExpiry();
    assertEquals (month, expiry.getMonthOfYear());
    assertEquals (day, expiry.getDayOfMonth());
    assertEquals (year, expiry.getYear());
    
    assertEquals (optionType, parser.getOptionType());
    assertEquals (strike, parser.getStrike());
  }
  
  // -------- ILLEGAL FORMATTING --------
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalIdentifierScheme () {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption(ExternalId.of(SecurityUtils.CUSIP, "12345678"));
  }
  
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern1 () {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "adddsfsdfsdf"));
  }
  
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern2 () {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption("dsfsdfds");
  }
  
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalOptionType () {
    BloombergTickerParserEQVanillaOption parser = 
      new BloombergTickerParserEQVanillaOption("MSFT US 01/21/12 X17.5 Equity");
  }
}
