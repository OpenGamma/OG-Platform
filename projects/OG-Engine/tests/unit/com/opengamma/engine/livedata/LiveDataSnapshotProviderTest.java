/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

import com.opengamma.core.security.impl.MockSecuritySource;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.livedata.test.TestLiveDataClient;

/**
 * Test LiveDataSnapshotProvider.
 */
public class LiveDataSnapshotProviderTest {

  private static final String _marketDataRequirement = MarketDataRequirementNames.MARKET_VALUE;
  
  UserPrincipal TEST_USER = new UserPrincipal("kirk", "127.0.0.1");
  UserPrincipal TEST_USER_2 = new UserPrincipal("alice", "127.0.0.1");
  
  protected ValueRequirement constructRequirement(String ticker) {
    return new ValueRequirement(_marketDataRequirement, ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("testdomain", ticker));
  }
  
  @Test
  public void snapshotting() {
    TestLiveDataClient client = new TestLiveDataClient();
    LiveDataSnapshotProviderImpl snapshotter = new LiveDataSnapshotProviderImpl(client, new MockSecuritySource());
    
    snapshotter.addSubscription(TEST_USER, constructRequirement("test1"));
    snapshotter.addSubscription(TEST_USER, constructRequirement("test2"));
    
    snapshotter.addSubscription(TEST_USER, constructRequirement("test3"));
    snapshotter.addSubscription(TEST_USER, constructRequirement("test3"));
    snapshotter.addSubscription(TEST_USER_2, constructRequirement("test3"));
    
    MutableFudgeFieldContainer msg1 = new FudgeContext().newMessage();
    msg1.add(_marketDataRequirement, 52.07);
    
    MutableFudgeFieldContainer msg2 = new FudgeContext().newMessage();
    msg2.add(_marketDataRequirement, 52.15);
    
    MutableFudgeFieldContainer msg3a = new FudgeContext().newMessage();
    msg3a.add(_marketDataRequirement, 52.16);
    MutableFudgeFieldContainer msg3b = new FudgeContext().newMessage();
    msg3b.add(_marketDataRequirement, 52.17);
    
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3b);
    
    long time = snapshotter.snapshot();
    
    Double test1Value = (Double) snapshotter.querySnapshot(time, constructRequirement("test1"));
    assertNotNull(test1Value);
    assertEquals(52.07, test1Value, 0.000001);
    
    Double test2Value = (Double) snapshotter.querySnapshot(time, constructRequirement("test2"));
    assertNotNull(test2Value);
    assertEquals(52.15, test2Value, 0.000001);
    
    Double test3Value = (Double) snapshotter.querySnapshot(time, constructRequirement("test3"));
    assertNotNull(test3Value);
    assertEquals(52.17, test3Value, 0.000001);
    
    assertNull(snapshotter.querySnapshot(time + 1, constructRequirement("test1")));
    assertNull(snapshotter.querySnapshot(time, constructRequirement("invalidticker")));
    
    snapshotter.releaseSnapshot(time);
    snapshotter.releaseSnapshot(time + 1); // no exception at the moment, hmm...
    
  }
  

}
