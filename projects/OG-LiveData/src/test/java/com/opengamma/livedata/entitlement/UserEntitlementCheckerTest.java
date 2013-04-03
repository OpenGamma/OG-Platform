/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.FixedDistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.security.user.Authority;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserGroup;
import com.opengamma.security.user.UserManager;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class UserEntitlementCheckerTest {

  public void basicPermissionCheck() {
    Set<UserGroup> userGroups = new HashSet<UserGroup>();

    UserGroup group1 = new UserGroup(0L, "group1");
    group1.getAuthorities().add(new Authority(0L, "LiveData/Bloomberg/Equity/**")); // ** -> all subpaths OK
    group1.getAuthorities().add(new Authority(1L, "LiveData/Bloomberg/Bond/*")); // * -> only 1 level of subpath OK
    userGroups.add(group1);
    
    UserGroup group2 = new UserGroup(1L, "group2");
    group2.getAuthorities().add(new Authority(2L, "LiveData/Reuters/Equity/**"));
    userGroups.add(group2);
    
    User user = new User(null,
        "john",
        "pw",
        userGroups,
        new Date());
    
    UserManager userManager = mock(UserManager.class);
    when(userManager.getUser("john")).thenReturn(user);
    
    DistributionSpecification aaplOnBloomberg = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "EQ12345"),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.Equity.AAPL"); 
    
    DistributionSpecification aaplOnBloombergWithNormalization = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "EQ12345"),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.Equity.AAPL.MyWeirdNormalizationRule"); 
    
    DistributionSpecification bondOnBloomberg = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "BOND12345"),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.Bond.IBMBOND123"); 
    
    DistributionSpecification bondOnBloombergWithNormalization = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "BOND12345"),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.Bond.IBMBOND123.MyWeirdNormalizationRule");
    
    DistributionSpecification fxOnBloomberg = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "FX12345"),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.FX.EURUSD");
    
    DistributionSpecification fxOnBloombergWithNormalization = new DistributionSpecification(
        ExternalId.of("BLOOMBERG_BUID", "FX12345"),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.FX.EURUSD.MyWeirdNormalizationRule");
    
    Map<LiveDataSpecification, DistributionSpecification> fixes = new HashMap<LiveDataSpecification, DistributionSpecification>();
    fixes.put(aaplOnBloomberg.getFullyQualifiedLiveDataSpecification(), aaplOnBloomberg);
    fixes.put(aaplOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification(), aaplOnBloombergWithNormalization);
    fixes.put(bondOnBloomberg.getFullyQualifiedLiveDataSpecification(), bondOnBloomberg);
    fixes.put(bondOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification(), bondOnBloombergWithNormalization);
    fixes.put(fxOnBloomberg.getFullyQualifiedLiveDataSpecification(), fxOnBloomberg);
    fixes.put(fxOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification(), fxOnBloombergWithNormalization);
    
    FixedDistributionSpecificationResolver resolver = new FixedDistributionSpecificationResolver(fixes);
    
    LiveDataEntitlementChecker userEntitlementChecker = new UserEntitlementChecker(userManager, resolver);
    
    UserPrincipal john = new UserPrincipal("john", "127.0.0.1");
    
    AssertJUnit.assertTrue(userEntitlementChecker.isEntitled(john, aaplOnBloomberg.getFullyQualifiedLiveDataSpecification()));
    AssertJUnit.assertTrue(userEntitlementChecker.isEntitled(john, aaplOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification()));
    AssertJUnit.assertTrue(userEntitlementChecker.isEntitled(john, bondOnBloomberg.getFullyQualifiedLiveDataSpecification()));
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(john, bondOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification()));
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(john, fxOnBloomberg.getFullyQualifiedLiveDataSpecification()));
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(john, fxOnBloombergWithNormalization.getFullyQualifiedLiveDataSpecification()));
    
    // non-existent user
    UserPrincipal mike = new UserPrincipal("mike", "127.0.0.1");
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(mike, aaplOnBloomberg.getFullyQualifiedLiveDataSpecification())); 
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(mike, fxOnBloomberg.getFullyQualifiedLiveDataSpecification()));
    
    // bogus spec
    AssertJUnit.assertFalse(userEntitlementChecker.isEntitled(john, 
        new LiveDataSpecification(
            StandardRules.getOpenGammaRuleSetId(), 
            ExternalId.of("RIC", "bar"))));
  }

}
