/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;

/**
 * Test {@link BloombergDomainIdentifierResolver}
 */
@Test(groups = "unit")
public class BloombergDomainIdentifierResolverTest {
  
  @Test
  public void toBloombergKeyWithDataProvider() {
    String bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"), "CMPL");
    assertNotNull(bbgKey);
    assertEquals("AAPL US EQUITY", bbgKey);
    
    bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(ExternalSchemes.bloombergTickerSecurityId("ADSW5Q Curncy"), "CMPL");
    assertNotNull(bbgKey);
    assertEquals("ADSW5Q CMPL CURNCY", bbgKey);
    
    bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(ExternalSchemes.bloombergTickerSecurityId("DJX Index"), "CMPL");
    assertNotNull(bbgKey);
    assertEquals("DJX@CMPL INDEX", bbgKey);
    
    bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(ExternalSchemes.bloombergBuidSecurityId("EO10169520130101C8800001"), "CMPL");
    assertNotNull(bbgKey);
    assertEquals("/buid/EO10169520130101C8800001@CMPL", bbgKey);
  }

}
