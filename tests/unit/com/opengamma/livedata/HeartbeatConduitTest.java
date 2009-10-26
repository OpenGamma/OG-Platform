/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Timer;

import org.fudgemsg.FudgeContext;
import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.livedata.client.ValueDistributor;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.ActiveSecurityPublicationManager;
import com.opengamma.livedata.server.HeartbeatReceiver;
import com.opengamma.transport.InMemoryByteArrayConduit;

/**
 * Integration test between {@link HeartbeatSender} and {@link HeartbeatReceiver}.
 *
 * @author kirk
 */
public class HeartbeatConduitTest {
  
  @Test
  public void singleSpecification() throws InterruptedException {
    AbstractLiveDataServer dataServer = new AbstractLiveDataServer() {
      
    };
    ActiveSecurityPublicationManager pubManager = new ActiveSecurityPublicationManager(dataServer);
    HeartbeatReceiver receiver = new HeartbeatReceiver(pubManager);
    InMemoryByteArrayConduit conduit = new InMemoryByteArrayConduit(receiver);
    ValueDistributor valueDistributor = new ValueDistributor();
    Timer t = new Timer("HeartbeatConduitTest");
    /*HeartbeatSender sender = */new HeartbeatSender(conduit, valueDistributor, new FudgeContext(), t, 1000l);
    // Wait at least 3 seconds to make sure we get no heartbeats.
    Thread.sleep(3000l);
    assertTrue(pubManager.getActiveSpecificationTimeouts().isEmpty());
    
    LiveDataSpecificationImpl subscription = new LiveDataSpecificationImpl(new DomainSpecificIdentifier(new IdentificationDomain("BbgId"), "USSw5 Curncy"));
    valueDistributor.addListener(subscription, new CollectingLiveDataListener());

    // Wait at least 3 seconds to make sure we get heartbeats.
    Thread.sleep(3000l);
    
    assertTrue(pubManager.isCurrentlyPublished(subscription));
    assertEquals(1, pubManager.getActiveSpecificationTimeouts().size());
    
    t.cancel();
  }

}
