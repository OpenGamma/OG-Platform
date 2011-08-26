/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;

/**
 * Test {@link SecurityUtils}.
 */
@Test
public class SecurityUtilsTest {

  public void test_constants() {
    assertEquals("ISIN", SecurityUtils.ISIN.getName());
    assertEquals("CUSIP", SecurityUtils.CUSIP.getName());
    assertEquals("SEDOL1", SecurityUtils.SEDOL1.getName());
    assertEquals("BLOOMBERG_BUID", SecurityUtils.BLOOMBERG_BUID.getName());
    assertEquals("BLOOMBERG_TICKER", SecurityUtils.BLOOMBERG_TICKER.getName());
    assertEquals("BLOOMBERG_TCM", SecurityUtils.BLOOMBERG_TCM.getName());
    assertEquals("RIC", SecurityUtils.RIC.getName());
  }

  public void test_identifiers() {
    assertEquals(ExternalId.of("ISIN", "A"), SecurityUtils.isinSecurityId("A"));
    assertEquals(ExternalId.of("CUSIP", "A"), SecurityUtils.cusipSecurityId("A"));
    assertEquals(ExternalId.of("SEDOL1", "A"), SecurityUtils.sedol1SecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_BUID", "A"), SecurityUtils.bloombergBuidSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TICKER", "A"), SecurityUtils.bloombergTickerSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TCM", "T 4.75 15/08/43 Govt"), SecurityUtils.bloombergTCMSecurityId("T", "4.75", "15/08/43", "Govt"));
    assertEquals(ExternalId.of("RIC", "A"), SecurityUtils.ricSecurityId("A"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isin_null() {
    SecurityUtils.isinSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_cusip_null() {
    SecurityUtils.cusipSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_sedol1_null() {
    SecurityUtils.sedol1SecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergBuid_null() {
    SecurityUtils.bloombergBuidSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergTicker_null() {
    SecurityUtils.bloombergTickerSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergTCM_null() {
    SecurityUtils.bloombergTCMSecurityId(null, null, null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ric_null() {
    SecurityUtils.ricSecurityId(null);
  }

}
