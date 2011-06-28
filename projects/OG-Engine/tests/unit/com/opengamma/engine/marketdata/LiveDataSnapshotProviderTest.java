/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.LiveMarketDataProvider;
import com.opengamma.engine.marketdata.availability.AllMarketDataAvailabilityProvider;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.livedata.test.TestLiveDataClient;

/**
 * Test LiveDataSnapshotProvider.
 */
@Test
public class LiveDataSnapshotProviderTest {

  private static final String _marketDataRequirement = MarketDataRequirementNames.MARKET_VALUE;
  
  UserPrincipal TEST_USER = new UserPrincipal("kirk", "127.0.0.1");
  UserPrincipal TEST_USER_2 = new UserPrincipal("alice", "127.0.0.1");
  
  protected ValueRequirement constructRequirement(String ticker) {
    return new ValueRequirement(_marketDataRequirement, ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("testdomain", ticker));
  }
  
  public void snapshotting() {
    TestLiveDataClient client = new TestLiveDataClient();
    LiveMarketDataProvider provider = new LiveMarketDataProvider(client, new PermissiveLiveDataEntitlementChecker(),
        new MockSecuritySource(), new AllMarketDataAvailabilityProvider());
    
    provider.subscribe(TEST_USER, constructRequirement("test1"));
    provider.subscribe(TEST_USER, constructRequirement("test2"));
    
    provider.subscribe(TEST_USER, constructRequirement("test3"));
    provider.subscribe(TEST_USER, constructRequirement("test3"));
    provider.subscribe(TEST_USER_2, constructRequirement("test3"));
    
    MutableFudgeMsg msg1 = new FudgeContext().newMessage();
    msg1.add(_marketDataRequirement, 52.07);
    
    MutableFudgeMsg msg2 = new FudgeContext().newMessage();
    msg2.add(_marketDataRequirement, 52.15);
    
    MutableFudgeMsg msg3a = new FudgeContext().newMessage();
    msg3a.add(_marketDataRequirement, 52.16);
    MutableFudgeMsg msg3b = new FudgeContext().newMessage();
    msg3b.add(_marketDataRequirement, 52.17);
    
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3b);
    
    MarketDataSnapshot snapshot = provider.snapshot(null);
    snapshot.init(Collections.<ValueRequirement>emptySet(), 0, TimeUnit.MILLISECONDS);
    
    Double test1Value = (Double) snapshot.query(constructRequirement("test1"));
    assertNotNull(test1Value);
    assertEquals(52.07, test1Value, 0.000001);
    
    Double test2Value = (Double) snapshot.query(constructRequirement("test2"));
    assertNotNull(test2Value);
    assertEquals(52.15, test2Value, 0.000001);
    
    Double test3Value = (Double) snapshot.query(constructRequirement("test3"));
    assertNotNull(test3Value);
    assertEquals(52.17, test3Value, 0.000001);
    
    assertNull(snapshot.query(constructRequirement("invalidticker")));
  }
  

}
