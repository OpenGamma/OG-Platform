/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.entitlement.UserEntitlementChecker;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.security.user.Authority;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserGroup;
import com.opengamma.security.user.UserManager;

/**
 * 
 *
 * @author pietari
 */
public class UserEntitlementCheckerTest {
  
  @Test
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
    
    UserEntitlementChecker userEntitlementChecker = new UserEntitlementChecker(userManager);
    
    DistributionSpecification aaplOnBloomberg = new DistributionSpecification(
        new IdentifierBundle(),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.Equity.AAPL"); 
    
    DistributionSpecification aaplOnBloombergWithNormalization = new DistributionSpecification(
        new IdentifierBundle(),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.Equity.AAPL.MyWeirdNormalizationRule"); 
    
    DistributionSpecification bondOnBloomberg = new DistributionSpecification(
        new IdentifierBundle(),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.Bond.IBMBOND123"); 
    
    DistributionSpecification bondOnBloombergWithNormalization = new DistributionSpecification(
        new IdentifierBundle(),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.Bond.IBMBOND123.MyWeirdNormalizationRule");

    DistributionSpecification fxOnBloomberg = new DistributionSpecification(
        new IdentifierBundle(),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.FX.EURUSD");
    
    DistributionSpecification fxOnBloombergWithNormalization = new DistributionSpecification(
        new IdentifierBundle(),
        new NormalizationRuleSet("MyWeirdNormalizationRule"),
        "LiveData.Bloomberg.FX.EURUSD.MyWeirdNormalizationRule");
    
    Assert.assertTrue(userEntitlementChecker.isEntitled("john", aaplOnBloomberg));
    Assert.assertTrue(userEntitlementChecker.isEntitled("john", aaplOnBloombergWithNormalization));
    Assert.assertTrue(userEntitlementChecker.isEntitled("john", bondOnBloomberg));
    Assert.assertFalse(userEntitlementChecker.isEntitled("john", bondOnBloombergWithNormalization));
    Assert.assertFalse(userEntitlementChecker.isEntitled("john", fxOnBloomberg));
    Assert.assertFalse(userEntitlementChecker.isEntitled("john", fxOnBloombergWithNormalization));
    
    // non-existent user
    Assert.assertFalse(userEntitlementChecker.isEntitled("mike", aaplOnBloomberg)); 
    Assert.assertFalse(userEntitlementChecker.isEntitled("mike", fxOnBloomberg)); 
  }

}
