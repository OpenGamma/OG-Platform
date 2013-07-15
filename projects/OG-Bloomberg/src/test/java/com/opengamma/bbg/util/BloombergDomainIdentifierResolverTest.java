/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergDomainIdentifierResolverTest {

  @DataProvider(name = "resolver")
  Object[][] data_resolver() {
    return new Object[][] {
        {ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"), "CMPL", "AAPL US Equity"},
        {ExternalSchemes.bloombergTickerSecurityId("ADSW5Q Curncy"), "CMPL", "ADSW5Q Curncy"},
        {ExternalSchemes.bloombergTickerSecurityId("DJX Index"), "CMPL", "DJX Index"},
        {ExternalSchemes.bloombergBuidSecurityId("EO10169520130101C8800001"), "CMPL", "/buid/EO10169520130101C8800001"},
        {ExternalSchemes.isinSecurityId("US0378331005"), "CMPL", "/isin/US0378331005"},
        {ExternalSchemes.cusipSecurityId("037833100"), "CMPL", "/cusip/037833100"},
    };
  }

  @Test(dataProvider = "resolver")
  public void toBloombergKey(ExternalId externalId, String ignoredDataProvider, String expectedBbgKey) {
    String bbgKey = BloombergDomainIdentifierResolver.toBloombergKey(externalId);
    assertNotNull(bbgKey);
    assertEquals(expectedBbgKey, bbgKey);
  }

  @DataProvider(name = "resolverWithProvider")
  Object[][] data_resolverWithProvider() {
    return new Object[][] {
        {ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"), "CMPL", "AAPL US EQUITY"},
        {ExternalSchemes.bloombergTickerSecurityId("ADSW5Q Curncy"), "CMPL", "ADSW5Q CMPL CURNCY"},
        {ExternalSchemes.bloombergTickerSecurityId("DJX Index"), "CMPL", "DJX CMPL INDEX"},
        {ExternalSchemes.bloombergBuidSecurityId("EO10169520130101C8800001"), "CMPL", "/buid/EO10169520130101C8800001@CMPL"},
        {ExternalSchemes.isinSecurityId("US0378331005"), "CMPL", "/isin/US0378331005@CMPL"},
        {ExternalSchemes.cusipSecurityId("037833100"), "CMPL", "/cusip/037833100@CMPL"},
    };
  }

  @Test(dataProvider = "resolverWithProvider")
  public void toBloombergKeyWithDataProvider(ExternalId externalId, String dataProvider, String expectedBbgKey) {
    String bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(externalId, dataProvider);
    assertNotNull(bbgKey);
    assertEquals(expectedBbgKey, bbgKey);
  }

}
