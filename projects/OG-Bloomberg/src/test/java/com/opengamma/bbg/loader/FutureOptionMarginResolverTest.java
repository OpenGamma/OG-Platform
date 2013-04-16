/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FutureOptionMarginResolverTest {

  @Test
  public void testCmeIsMargined() {
    checkResult("CME", true);
  }

  @Test
  public void testUsIsMargined() {
    checkResult("US", true);
  }

  @Test
  public void testCboeIsMargined() {
    checkResult("CBOE", true);
  }

  @Test
  public void testCbotIsMargined() {
    checkResult("CBOT", true);
  }

  @Test
  public void testLiffeIsMargined() {
    checkResult("LIF", true);
  }

  @Test
  public void testEurexIsMargined() {
    checkResult("EUX", true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testUnknownExchangeThrowsException() {
    new FutureOptionMarginResolver().isMargined("NOT_AN_EXCHANGE");
  }

  private void checkResult(String exchangeCode, boolean expected) {
    Assert.assertEquals(new FutureOptionMarginResolver().isMargined(exchangeCode), expected);
  }

}
