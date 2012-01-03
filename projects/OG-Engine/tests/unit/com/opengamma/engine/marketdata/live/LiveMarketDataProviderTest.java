/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.livedata.test.TestLiveDataClient;

/**
 * Test LiveDataSnapshotProvider.
 */
@Test
public class LiveMarketDataProviderTest {

  private static final String _marketDataRequirement = MarketDataRequirementNames.MARKET_VALUE;
  
  UserPrincipal TEST_USER = new UserPrincipal("kirk", "127.0.0.1");
  UserPrincipal TEST_USER_2 = new UserPrincipal("alice", "127.0.0.1");
  
  protected ValueRequirement constructRequirement(String ticker) {
    return new ValueRequirement(_marketDataRequirement, ComputationTargetType.PRIMITIVE, UniqueId.of("testdomain", ticker));
  }
  
  public void snapshotting() {
    ValueRequirement test1Requirement = constructRequirement("test1");
    ValueRequirement test2Requirement = constructRequirement("test2");
    ValueRequirement test3Requirement = constructRequirement("test3");
    
    TestLiveDataClient client = new TestLiveDataClient();
    FixedMarketDataAvailabilityProvider availabilityProvider = new FixedMarketDataAvailabilityProvider();
    availabilityProvider.addAvailableRequirement(test1Requirement);
    availabilityProvider.addAvailableRequirement(test2Requirement);
    availabilityProvider.addAvailableRequirement(test3Requirement);
    LiveMarketDataProvider provider = new LiveMarketDataProvider(client, new MockSecuritySource(), availabilityProvider);
    
    provider.subscribe(TEST_USER, test1Requirement);
    provider.subscribe(TEST_USER, test2Requirement);
    
    provider.subscribe(TEST_USER, test3Requirement);
    provider.subscribe(TEST_USER, test3Requirement);
    provider.subscribe(TEST_USER_2, test3Requirement);
    
    MutableFudgeMsg msg1 = new FudgeContext().newMessage();
    msg1.add(_marketDataRequirement, 52.07);
    
    MutableFudgeMsg msg2 = new FudgeContext().newMessage();
    msg2.add(_marketDataRequirement, 52.15);
    
    MutableFudgeMsg msg3a = new FudgeContext().newMessage();
    msg3a.add(_marketDataRequirement, 52.16);
    MutableFudgeMsg msg3b = new FudgeContext().newMessage();
    msg3b.add(_marketDataRequirement, 52.17);
    
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test3")), msg3b);
    
    MarketDataSnapshot snapshot = provider.snapshot(null);
    snapshot.init(Collections.<ValueRequirement>emptySet(), 0, TimeUnit.MILLISECONDS);
    
    Double test1Value = (Double) snapshot.query(test1Requirement);
    assertNotNull(test1Value);
    assertEquals(52.07, test1Value, 0.000001);
    
    Double test2Value = (Double) snapshot.query(test2Requirement);
    assertNotNull(test2Value);
    assertEquals(52.15, test2Value, 0.000001);
    
    Double test3Value = (Double) snapshot.query(test3Requirement);
    assertNotNull(test3Value);
    assertEquals(52.17, test3Value, 0.000001);
    
    assertNull(snapshot.query(constructRequirement("invalidticker")));
  }
  

}
