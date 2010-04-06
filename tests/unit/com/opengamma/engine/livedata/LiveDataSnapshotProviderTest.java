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
import com.opengamma.engine.value.MarketDataFieldNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.TestLiveDataClient;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSnapshotProviderTest {
  
  protected ValueRequirement constructRequirement(String ticker) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.PRIMITIVE, new DomainSpecificIdentifier("testdomain", ticker));
  }
  
  @Test
  public void snapshotting() {
    TestLiveDataClient client = new TestLiveDataClient();
    LiveDataSnapshotProviderImpl snapshotter = new LiveDataSnapshotProviderImpl(client);
    
    snapshotter.addSubscription("Test User", constructRequirement("test1"));
    snapshotter.addSubscription("Test User", constructRequirement("test2"));
    
    snapshotter.addSubscription("Test User", constructRequirement("test3"));
    snapshotter.addSubscription("Test User", constructRequirement("test3"));
    snapshotter.addSubscription("Test User 2", constructRequirement("test3"));
    
    MutableFudgeFieldContainer bidask1 = new FudgeContext().newMessage();
    bidask1.add(LiveDataSnapshotProviderImpl.BID_FIELD, 52.07);
    bidask1.add(LiveDataSnapshotProviderImpl.ASK_FIELD, 52.10);
    
    MutableFudgeFieldContainer last2 = new FudgeContext().newMessage();
    last2.add(LiveDataSnapshotProviderImpl.LAST_PRICE_FIELD, 52.15);
    
    MutableFudgeFieldContainer last3a = new FudgeContext().newMessage();
    last3a.add(LiveDataSnapshotProviderImpl.LAST_PRICE_FIELD, 52.16);
    MutableFudgeFieldContainer last3b = new FudgeContext().newMessage();
    last3b.add(LiveDataSnapshotProviderImpl.LAST_PRICE_FIELD, 52.17);
    
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), new DomainSpecificIdentifier("testdomain", "test1")), bidask1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), new DomainSpecificIdentifier("testdomain", "test2")), last2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), new DomainSpecificIdentifier("testdomain", "test3")), last3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), new DomainSpecificIdentifier("testdomain", "test3")), last3b);
    
    long time = snapshotter.snapshot();
    
    FudgeFieldContainer snapshot1 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test1"));
    assertNotNull(snapshot1);
    assertEquals(52.085, snapshot1.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME), 0.000001);
    
    FudgeFieldContainer snapshot2 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test2"));
    assertNotNull(snapshot2);
    assertEquals(52.15, snapshot2.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME), 0.000001);
    
    FudgeFieldContainer snapshot3 = (FudgeFieldContainer) snapshotter.querySnapshot(time, constructRequirement("test3"));
    assertNotNull(snapshot3);
    assertEquals(52.17, snapshot3.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME), 0.000001);
    
    assertNull(snapshotter.querySnapshot(time + 1, constructRequirement("test1")));
    assertNull(snapshotter.querySnapshot(time, constructRequirement("invalidticker")));
    
    snapshotter.releaseSnapshot(time);
    snapshotter.releaseSnapshot(time + 1); // no exception at the moment, hmm...
    
  }
  

}
