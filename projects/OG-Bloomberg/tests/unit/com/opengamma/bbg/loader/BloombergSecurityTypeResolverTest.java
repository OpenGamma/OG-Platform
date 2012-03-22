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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

/**
 * Test BloombergSecurityTypeResolver.
 */
public class BloombergSecurityTypeResolverTest {
  
  private ConfigurableApplicationContext _context;
  private SecurityTypeResolver _securityTypeResolver;
  

  @BeforeMethod
  public void setUp() throws Exception {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("/com/opengamma/bbg/loader/refDataProvider-context.xml");
    context.start();
    _context = context;
    ReferenceDataProvider refDataProvider = _context.getBean("cachingRefProvider", ReferenceDataProvider.class);
    _securityTypeResolver = new BloombergSecurityTypeResolver(refDataProvider);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_context != null) {
      _context.stop();
      _context = null;
    }
    _securityTypeResolver = null;
  }

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
  public void testBondSecurity() {
    assertSecurityType(Collections.singleton("GV912810EL8"), SecurityUtils.BLOOMBERG_BUID, SecurityType.BOND);
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
    assertSecurityType(identifiers, SecurityUtils.BLOOMBERG_TICKER, securityType);
  }

}
