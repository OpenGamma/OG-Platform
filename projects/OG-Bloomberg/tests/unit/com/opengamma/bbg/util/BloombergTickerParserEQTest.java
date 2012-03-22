/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;


public class BloombergTickerParserEQTest {
  //-------- BASIC CASES --------
  @Test
  public void testWithTickerIdentifier() {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ (ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "MSFT US Equity"));;
    assertEquals ("US", parser.getExchangeCode());
    assertEquals ("MSFT", parser.getSymbol());
  }
  
  @Test
  public void testWithTickerString() {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ ("MSFT US Equity");
    assertEquals ("US", parser.getExchangeCode());
    assertEquals ("MSFT", parser.getSymbol());
  }
  
  // -------- ILLEGAL FORMATTING --------
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalIdentifierScheme () {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ(ExternalId.of(SecurityUtils.CUSIP, "12345678"));
  }
  
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern1 () {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "adddsfsdfsdf"));
  }
  
  @Test (expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern2 () {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ("dsfsdfds");
  }
  
}
