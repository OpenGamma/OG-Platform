/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.util.MockReferenceDataProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergIdResolverTest {

  static final String AAPL_BB_ID_UNIQUE = "EQ0010169500001000";
  private MockReferenceDataProvider _refDataProvider;

  @BeforeMethod
  public void setup() {
    _refDataProvider = new MockReferenceDataProvider();
    _refDataProvider.addResult("AAPL US Equity", BloombergConstants.FIELD_ID_BBG_UNIQUE, AAPL_BB_ID_UNIQUE);
    _refDataProvider.addResult("foo123", null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void aaplNoBbgId() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(ExternalSchemes.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }

  @Test
  public void aaplWithBbgId() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"),
        ExternalSchemes.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(ExternalSchemes.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }

  @Test
  public void bbgIdOnly() {
    ExternalIdBundle aaplEquity = ExternalIdBundle.of(ExternalSchemes.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(aaplEquity);
    assertEquals(ExternalSchemes.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE), resolved);
  }

  @Test
  public void invalidBbgId() {
    ExternalIdBundle invalidSpec = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("foo123"));
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(invalidSpec);
    assertNull(resolved);
  }

  @Test
  public void invalidBbgUniqueId() {
    ExternalId invalidSpec = ExternalSchemes.bloombergBuidSecurityId("foo123");
    BloombergIdResolver resolver = new BloombergIdResolver(_refDataProvider);
    ExternalId resolved = resolver.resolve(ExternalIdBundle.of(invalidSpec));
    
    // doesn't validate unique IDs at the moment! should it?
    assertEquals(invalidSpec, resolved);
  }

}
