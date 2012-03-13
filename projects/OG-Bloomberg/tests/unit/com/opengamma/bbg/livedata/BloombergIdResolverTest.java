/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Test.
 */
public class BloombergIdResolverTest {

  /**
   * 
   */
  static final String AAPL_BB_ID_UNIQUE = "EQ0010169500001000";
  private CachingReferenceDataProvider _refDataProvider = null;

  @BeforeMethod
  public void setupBloombergSecurityMaster(Method m) {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(m);
  }
  
  @AfterMethod
  public void terminateSecurityMaster() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

  //-------------------------------------------------------------------------
  @Test
  public void aaplNoBbgId() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("AAPL US Equity"));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }

  @Test
  public void aaplWithBbgId() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId("AAPL US Equity"),
        SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }

  @Test
  public void bbgIdOnly() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }
  
  @Test
  public void invalidBbgId() {
    ExternalIdBundle invalidSpec = ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("foo123"));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(invalidSpec);
    assertNull(resolved);
  }
  
  @Test
  public void invalidBbgUniqueId() {
    ExternalId invalidSpec = SecurityUtils.bloombergBuidSecurityId("foo123");
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(ExternalIdBundle.of(invalidSpec));
    
    // doesn't validate unique IDs at the moment! should it?
    assertEquals(invalidSpec, resolved);
  }
}
