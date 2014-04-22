/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalSchemes}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalSchemesTest {

  @SuppressWarnings("deprecation")
  public void test_constants() {
    assertEquals("ISIN", ExternalSchemes.ISIN.getName());
    assertEquals("CUSIP", ExternalSchemes.CUSIP.getName());
    assertEquals("SEDOL1", ExternalSchemes.SEDOL1.getName());
    assertEquals("BLOOMBERG_BUID", ExternalSchemes.BLOOMBERG_BUID.getName());
    assertEquals("BLOOMBERG_TICKER", ExternalSchemes.BLOOMBERG_TICKER.getName());
    assertEquals("BLOOMBERG_TCM", ExternalSchemes.BLOOMBERG_TCM.getName());
    assertEquals("RIC", ExternalSchemes.RIC.getName());
    assertEquals("MARKIT_RED_CODE", ExternalSchemes.MARKIT_RED_CODE.getName());
    assertEquals("ISDA", ExternalSchemes.ISDA.getName());
    assertEquals("BLOOMBERG_UUID", ExternalSchemes.BLOOMBERG_UUID.getName());
    assertEquals("BLOOMBERG_EMRSID", ExternalSchemes.BLOOMBERG_EMRSID.getName());
  }

  public void test_identifiers() {
    assertEquals(ExternalId.of("ISIN", "A"), ExternalSchemes.isinSecurityId("A"));
    assertEquals(ExternalId.of("CUSIP", "A"), ExternalSchemes.cusipSecurityId("A"));
    assertEquals(ExternalId.of("SEDOL1", "A"), ExternalSchemes.sedol1SecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_BUID", "A"), ExternalSchemes.bloombergBuidSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TICKER", "A"), ExternalSchemes.bloombergTickerSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TCM", "T 4.75 15/08/43 Govt"), ExternalSchemes.bloombergTCMSecurityId("T", "4.75", "15/08/43", "Govt"));
    assertEquals(ExternalId.of("RIC", "A"), ExternalSchemes.ricSecurityId("A"));
    assertEquals(ExternalId.of("MARKIT_RED_CODE", "A"), ExternalSchemes.markItRedCode("A"));
    assertEquals(ExternalId.of("ISDA", "A"), ExternalSchemes.isda("A"));
    assertEquals(ExternalId.of("BLOOMBERG_UUID", "A"), ExternalSchemes.bloombergUUIDUserId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_EMRSID", "A"), ExternalSchemes.bloombergEMRSUserId("A"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isin_null() {
    ExternalSchemes.isinSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_cusip_null() {
    ExternalSchemes.cusipSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_sedol1_null() {
    ExternalSchemes.sedol1SecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergBuid_null() {
    ExternalSchemes.bloombergBuidSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergUuid_null() {
    ExternalSchemes.bloombergUUIDUserId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergEmrsid_null() {
    ExternalSchemes.bloombergEMRSUserId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergTicker_null() {
    ExternalSchemes.bloombergTickerSecurityId(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bloombergTCM_null() {
    ExternalSchemes.bloombergTCMSecurityId(null, null, null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ric_null() {
    ExternalSchemes.ricSecurityId(null);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_markitRedCode_null() {
    ExternalSchemes.markItRedCode(null);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isda_null() {
    ExternalSchemes.isda(null);
  }
  
}
