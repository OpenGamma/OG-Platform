/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.security.MockSecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.TestLiveDataClient;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Test LiveDataSnapshotProvider.
 */
public class LiveDataSnapshotProviderTest {

  UserPrincipal TEST_USER = new UserPrincipal("kirk", "127.0.0.1");
  UserPrincipal TEST_USER_2 = new UserPrincipal("alice", "127.0.0.1");
  
  protected ValueRequirement constructRequirement(String ticker) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("testdomain", ticker));
  }
  
  @Test
  public void snapshotting() {
    TestLiveDataClient client = new TestLiveDataClient();
    LiveDataSnapshotProviderImpl snapshotter = new LiveDataSnapshotProviderImpl(client, new MockSecurityMaster());
    
    snapshotter.addSubscription(TEST_USER, constructRequirement("test1"));
    snapshotter.addSubscription(TEST_USER, constructRequirement("test2"));
    
    snapshotter.addSubscription(TEST_USER, constructRequirement("test3"));
    snapshotter.addSubscription(TEST_USER, constructRequirement("test3"));
    snapshotter.addSubscription(TEST_USER_2, constructRequirement("test3"));
    
    MutableFudgeFieldContainer msg1 = new FudgeContext().newMessage();
    msg1.add("Foo", 52.07);
    
    MutableFudgeFieldContainer msg2 = new FudgeContext().newMessage();
    msg2.add("Foo", 52.15);
    
    MutableFudgeFieldContainer msg3a = new FudgeContext().newMessage();
    msg3a.add("Foo", 52.16);
    MutableFudgeFieldContainer msg3b = new FudgeContext().newMessage();
    msg3b.add("Foo", 52.17);
    
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), Identifier.of("testdomain", "test3")), msg3b);
    
    long time = snapshotter.snapshot();
    
    FudgeFieldContainer snapshot1 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test1"));
    assertNotNull(snapshot1);
    assertEquals(52.07, snapshot1.getDouble("Foo"), 0.000001);
    
    FudgeFieldContainer snapshot2 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test2"));
    assertNotNull(snapshot2);
    assertEquals(52.15, snapshot2.getDouble("Foo"), 0.000001);
    
    FudgeFieldContainer snapshot3 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test3"));
    assertNotNull(snapshot3);
    assertEquals(52.17, snapshot3.getDouble("Foo"), 0.000001);
    
    assertNull(snapshotter.querySnapshot(time + 1, constructRequirement("test1")));
    assertNull(snapshotter.querySnapshot(time, constructRequirement("invalidticker")));
    
    snapshotter.releaseSnapshot(time);
    snapshotter.releaseSnapshot(time + 1); // no exception at the moment, hmm...
    
  }
  

}
