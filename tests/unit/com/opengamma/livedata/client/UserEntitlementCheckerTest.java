/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.LiveDataSpecification;
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
  public void testBasicPermissionCheck() {
    
    Set<UserGroup> userGroups = new HashSet<UserGroup>();

    UserGroup group1 = new UserGroup(0L, "group1");
    group1.getAuthorities().add(new Authority(0L, "/MarketData/Bloomberg/EQ*"));
    userGroups.add(group1);
    
    UserGroup group2 = new UserGroup(1L, "group2");
    group2.getAuthorities().add(new Authority(1L, "/MarketData/Reuters/EQ*"));
    userGroups.add(group2);
    
    User user = new User(null,
        "john",
        "pw",
        userGroups,
        new Date());
    
    UserManager userManager = mock(UserManager.class);
    when(userManager.getUser("john")).thenReturn(user);
    
    IdentificationDomain bbguidDomain = new IdentificationDomain("bbguid");
    UserEntitlementChecker userEntitlementChecker = new UserEntitlementChecker(
        userManager,
        "/MarketData/Bloomberg",
        bbguidDomain
        );
    
    LiveDataSpecification appleStockMarketDataOnBloomberg = new LiveDataSpecification(new DomainSpecificIdentifier(bbguidDomain, "EQ0010169500001000"));
    LiveDataSpecification someFxDataOnBloomberg = new LiveDataSpecification(new DomainSpecificIdentifier(bbguidDomain, "FX123456"));
    
    Assert.assertTrue(userEntitlementChecker.isEntitled("john", appleStockMarketDataOnBloomberg));
    Assert.assertFalse(userEntitlementChecker.isEntitled("john", someFxDataOnBloomberg));
    
    Assert.assertFalse(userEntitlementChecker.isEntitled("mike", appleStockMarketDataOnBloomberg));
    Assert.assertFalse(userEntitlementChecker.isEntitled("mike", someFxDataOnBloomberg));
  }

}
