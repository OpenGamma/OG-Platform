/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergTickerParserEQTest {

  //-------- BASIC CASES --------
  @Test
  public void testWithTickerIdentifier() {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "MSFT US Equity"));
    assertEquals("US", parser.getExchangeCode());
    assertEquals("MSFT", parser.getSymbol());
  }

  @Test
  public void testWithTickerString() {
    BloombergTickerParserEQ parser = new BloombergTickerParserEQ("MSFT US Equity");
    assertEquals("US", parser.getExchangeCode());
    assertEquals("MSFT", parser.getSymbol());
  }

  // -------- ILLEGAL FORMATTING --------
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalIdentifierScheme() {
    new BloombergTickerParserEQ(ExternalId.of(ExternalSchemes.CUSIP, "12345678"));
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern1() {
    new BloombergTickerParserEQ(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "adddsfsdfsdf"));
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern2() {
    new BloombergTickerParserEQ("dsfsdfds");
  }

}
