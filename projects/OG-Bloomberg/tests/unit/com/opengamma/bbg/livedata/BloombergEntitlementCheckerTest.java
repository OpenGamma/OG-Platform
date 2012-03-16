/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.FixedDistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * 
 */
@Test(enabled = false)
public class BloombergEntitlementCheckerTest {
  
  public BloombergEntitlementCheckerTest() {
  }

  static final String AAPL_BB_ID_UNIQUE = "EQ0010169500001000";
  static final ExternalId AAPL_EQUITY = SecurityUtils.bloombergBuidSecurityId(AAPL_BB_ID_UNIQUE);
  static final DistributionSpecification DIST_SPEC = 
    new DistributionSpecification(AAPL_EQUITY, StandardRules.getNoNormalization(), "AAPL");
  
  private LiveDataEntitlementChecker _entitlementChecker;
  
  @BeforeClass
  public void setUpClass() {
    SessionOptions sessionOptions = BloombergTestUtils.getSessionOptions();
    BloombergReferenceDataProvider rdp = new BloombergReferenceDataProvider(sessionOptions);
    rdp.start();
    
    Map<LiveDataSpecification, DistributionSpecification> fixes = new HashMap<LiveDataSpecification, DistributionSpecification>();
    fixes.put(DIST_SPEC.getFullyQualifiedLiveDataSpecification(), DIST_SPEC);    
    FixedDistributionSpecificationResolver resolver = new FixedDistributionSpecificationResolver(fixes);
    
    BloombergEntitlementChecker entitlementChecker = new BloombergEntitlementChecker(sessionOptions, rdp, resolver);
    entitlementChecker.start();
    _entitlementChecker = entitlementChecker;
  }
  
  public void entitled() throws Exception {
    UserPrincipal user = new UserPrincipal("6926421", InetAddress.getLocalHost().getHostAddress());
    assertTrue(_entitlementChecker.isEntitled(user, DIST_SPEC.getFullyQualifiedLiveDataSpecification()));
  }
  
  public void notEntitled() {
    UserPrincipal user = new UserPrincipal("impostor", "127.0.0.1");
    assertFalse(_entitlementChecker.isEntitled(user, DIST_SPEC.getFullyQualifiedLiveDataSpecification()));
  }

}
