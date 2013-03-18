/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergTickerParserEQVanillaOptionTest {

  //-------- BASIC CASES --------
  @Test
  public void testWithTickerIdentifier() {
    BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(
        ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "MSFT US 01/21/12 C17.5 Equity"));
    testImpl(parser, "MSFT", Month.JANUARY, 21, 2012, OptionType.CALL, 17.5);
  }

  @Test
  public void testWithTickerString() {
    BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption("MSFT US 01/21/12 C17.5 Equity");
    testImpl(parser, "MSFT", Month.JANUARY, 21, 2012, OptionType.CALL, 17.5);
  }

  @Test
  public void testOtherTickerPatternBug() {
    BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption("AAPL US 01/19/13 C135 Equity");
    testImpl(parser, "AAPL", Month.JANUARY, 19, 2013, OptionType.CALL, 135);
  }

  private void testImpl(
      BloombergTickerParserEQVanillaOption parser, String symbol,
      Month month, int day, int year,
      OptionType optionType, double strike) {
    assertEquals("US", parser.getExchangeCode());
    assertEquals(symbol, parser.getSymbol());

    LocalDate expiry = parser.getExpiry();
    assertEquals(month, expiry.getMonth());
    assertEquals(day, expiry.getDayOfMonth());
    assertEquals(year, expiry.getYear());

    assertEquals(optionType, parser.getOptionType());
    assertEquals(strike, parser.getStrike());
  }

  // -------- ILLEGAL FORMATTING --------
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalIdentifierScheme() {
    new BloombergTickerParserEQVanillaOption(ExternalId.of(ExternalSchemes.CUSIP, "12345678"));
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern1() {
    new BloombergTickerParserEQVanillaOption(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "adddsfsdfsdf"));
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalPattern2() {
    new BloombergTickerParserEQVanillaOption("dsfsdfds");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testIllegalOptionType() {
    new BloombergTickerParserEQVanillaOption("MSFT US 01/21/12 X17.5 Equity");
  }

}
