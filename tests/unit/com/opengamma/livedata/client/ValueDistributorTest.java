/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.CollectingLiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;

/**
 * 
 *
 * @author kirk
 */
public class ValueDistributorTest {
  private final FudgeContext _fudgeContext = new FudgeContext();
  
  @Test
  public void activeSpecificationsOneSpec() {
    ValueDistributor distributor = new ValueDistributor();
    Set<LiveDataSpecification> activeSpecs = null;
    
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    CollectingLiveDataListener listener2 = new CollectingLiveDataListener();
    LiveDataSpecification spec = new LiveDataSpecification("foo", new Identifier(new IdentificationScheme("bar"), "baz"));
    
    distributor.addListener(spec, listener1);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertEquals(1, activeSpecs.size());
    assertTrue(activeSpecs.contains(spec));

    distributor.addListener(spec, listener2);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertEquals(1, activeSpecs.size());
    assertTrue(activeSpecs.contains(spec));
    
    distributor.removeListener(spec, listener1);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertEquals(1, activeSpecs.size());
    assertTrue(activeSpecs.contains(spec));

    distributor.removeListener(spec, listener2);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertTrue(activeSpecs.isEmpty());
  }

  @Test
  public void activeSpecificationsTwoSpecs() {
    ValueDistributor distributor = new ValueDistributor();
    Set<LiveDataSpecification> activeSpecs = null;
    
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    LiveDataSpecification spec1 = new LiveDataSpecification("x", new Identifier(new IdentificationScheme("foo"), "bar1"));
    LiveDataSpecification spec2 = new LiveDataSpecification("x", new Identifier(new IdentificationScheme("foo"), "bar2"));
    
    distributor.addListener(spec1, listener1);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertEquals(1, activeSpecs.size());
    assertTrue(activeSpecs.contains(spec1));

    distributor.addListener(spec2, listener1);
    activeSpecs = distributor.getActiveSpecifications();
    assertNotNull(activeSpecs);
    assertEquals(2, activeSpecs.size());
    assertTrue(activeSpecs.contains(spec1));
    assertTrue(activeSpecs.contains(spec2));
  }
  
  @Test
  public void simpleDistribution() {
    ValueDistributor distributor = new ValueDistributor();
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    LiveDataSpecification spec1 = new LiveDataSpecification("foo", new Identifier(new IdentificationScheme("bar"), "baz"));
    
    distributor.addListener(spec1, listener1);
    
    long timestamp = System.currentTimeMillis();
    distributor.notifyListeners(timestamp, spec1, _fudgeContext.newMessage());
    
    List<LiveDataValueUpdate> updates = listener1.getValueUpdates();
    assertEquals(1, updates.size());
    LiveDataValueUpdate update = updates.get(0);
    assertEquals(timestamp, update.getRelevantTimestamp());
    assertEquals(spec1, update.getSpecification());
    assertNotNull(update.getFields());
  }

}
