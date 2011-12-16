/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class ValueDistributorTest {
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  
  @Test
  public void activeSpecificationsOneSpec() {
    ValueDistributor distributor = new ValueDistributor();
    Set<LiveDataSpecification> activeSpecs = null;
    
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    CollectingLiveDataListener listener2 = new CollectingLiveDataListener();
    LiveDataSpecification spec = new LiveDataSpecification("foo", ExternalId.of("bar", "baz"));
    
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
    LiveDataSpecification spec1 = new LiveDataSpecification("x", ExternalId.of("foo", "bar1"));
    LiveDataSpecification spec2 = new LiveDataSpecification("x", ExternalId.of("foo", "bar2"));
    
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
    LiveDataSpecification spec1 = new LiveDataSpecification("foo", ExternalId.of("bar", "baz"));
    
    distributor.addListener(spec1, listener1);
    
    long sequenceNumber = 12345;
    distributor.notifyListeners(new LiveDataValueUpdateBean(sequenceNumber, spec1, _fudgeContext.newMessage()));
    
    List<LiveDataValueUpdate> updates = listener1.getValueUpdates();
    assertEquals(1, updates.size());
    LiveDataValueUpdate update = updates.get(0);
    assertEquals(sequenceNumber, update.getSequenceNumber());
    assertEquals(spec1, update.getSpecification());
    assertNotNull(update.getFields());
  }

}
