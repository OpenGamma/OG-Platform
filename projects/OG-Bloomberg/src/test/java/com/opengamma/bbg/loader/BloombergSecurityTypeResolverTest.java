/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergSecurityTypeResolverTest {

  private BloombergReferenceDataProvider _bbgProvider;
  private SecurityTypeResolver _securityTypeResolver;

  @BeforeClass
  public void setUpClass() throws Exception {
    _bbgProvider = BloombergTestUtils.getBloombergReferenceDataProvider();
    _bbgProvider.start();
    ReferenceDataProvider cachingProvider = BloombergTestUtils.getMongoCachingReferenceDataProvider(_bbgProvider);
    _securityTypeResolver = new BloombergSecurityTypeResolver(cachingProvider);
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    if (_bbgProvider != null) {
      _bbgProvider.stop();
      _bbgProvider = null;
    }
    _securityTypeResolver = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void testEquity() {
    Set<String> testEquities = Sets.newHashSet("AAPL US Equity", "UNG US Equity");
    assertSecurityType(testEquities, SecurityType.EQUITY);
  }

  @Test
  public void testEquityOption() {
    assertSecurityType(Collections.singleton("APV US 01/16/10 C190 Equity"), SecurityType.EQUITY_OPTION);
  }

  @Test
  public void testEquityIndexOption() {
    assertSecurityType(Collections.singleton("SPX US 12/18/10 C1100 Index"), SecurityType.EQUITY_INDEX_OPTION);
  }

  @Test
  public void testEquityIndexFutureOption() {
    assertSecurityType(Collections.singleton("ESH3C 1000 Index"), SecurityType.EQUITY_INDEX_FUTURE_OPTION);
  }

  @Test
  public void testEquityIndexDividendFutureOption() {
    assertSecurityType(Collections.singleton("DEDZ3C 100.00 Index"), SecurityType.EQUITY_INDEX_DIVIDEND_FUTURE_OPTION);
  }

  @Test
  public void testBondFuture() {
    assertSecurityType(Collections.singleton("USM10 Comdty"), SecurityType.BOND_FUTURE);
  }

  @Test
  public void testInterestRateFuture() {
    assertSecurityType(Collections.singleton("EDM10 Comdty"), SecurityType.INTEREST_RATE_FUTURE);
  }

  @Test
  public void testIRFutureOptionSecurity() {
    assertSecurityType(Collections.singleton("EDZ2C 99.500 Comdty"), SecurityType.IR_FUTURE_OPTION);
  }

  @Test
  public void testCommodityFutureOptionSecurity() {
    assertSecurityType(Collections.singleton("CHH3C 24.25 Comdty"), SecurityType.COMMODITY_FUTURE_OPTION);
  }

  @Test
  public void testFxFutureOptionSecurity() {
    assertSecurityType(Collections.singleton("JYH3P 105.0 Curncy"), SecurityType.FX_FUTURE_OPTION);
  }

  @Test
  public void testBondSecurity() {
    assertSecurityType(Collections.singleton("GV912810EL8"), ExternalSchemes.BLOOMBERG_BUID, SecurityType.BOND);
  }

  private void assertSecurityType(final Set<String> identifiers, final ExternalScheme scheme, final SecurityType securityType) {
    Set<ExternalIdBundle> testBundles = Sets.newHashSet();
    for (String identifierValue : identifiers) {
      testBundles.add(ExternalIdBundle.of(ExternalId.of(scheme, identifierValue)));
    }
    Map<ExternalIdBundle, SecurityType> searchResults = _securityTypeResolver.getSecurityType(testBundles);
    assertNotNull(searchResults);
    for (ExternalIdBundle identifier : testBundles) {
      assertEquals(securityType, searchResults.get(identifier));
    }
  }

  private void assertSecurityType(final Set<String> identifiers, final SecurityType securityType) {
    assertSecurityType(identifiers, ExternalSchemes.BLOOMBERG_TICKER, securityType);
  }

}
