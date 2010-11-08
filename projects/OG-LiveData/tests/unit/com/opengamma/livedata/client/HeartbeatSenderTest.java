/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.transport.CollectingByteArrayMessageSender;

/**
 *
 * @author kirk
 */
public class HeartbeatSenderTest {
  private Timer _timer = null;

  @Before
  public void startTimer() {
    _timer = new Timer("HeartbeatSenderTest Timer");
  }
  
  @After
  public void shutdownTimer() {
    _timer.cancel();
    _timer = null;
  }
  
  @Test
  public void basicOperation() throws InterruptedException, IOException {
    CollectingByteArrayMessageSender messageSender = new CollectingByteArrayMessageSender();
    ValueDistributor valueDistributor = new ValueDistributor();
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    LiveDataSpecification spec1 = new LiveDataSpecification(
        "Test",
        Identifier.of("foo", "bar"));
    LiveDataSpecification spec2 = new LiveDataSpecification(
        "Test",
        Identifier.of("foo", "baz"));
    valueDistributor.addListener(spec1, listener1);
    valueDistributor.addListener(spec2, listener1);
    
    @SuppressWarnings("unused")
    HeartbeatSender heartbeatSender = new HeartbeatSender(messageSender, valueDistributor, new FudgeContext(), _timer, 100l);
    // Wait 250ms to make sure we get two ticks.
    Thread.sleep(250l);
    
    List<byte[]> messages = messageSender.getMessages();
    assertTrue(messages.size() >= 2);
    
    FudgeContext fudgeContext = new FudgeContext();
    
    for(byte[] message : messages) {
      FudgeMsgEnvelope fudgeMsgEnvelope = fudgeContext.deserialize(message);
      FudgeFieldContainer fudgeMsg = fudgeMsgEnvelope.getMessage();
      assertNotNull(fudgeMsg);
      assertEquals(2, fudgeMsg.getNumFields());
      for(FudgeField field : fudgeMsg.getAllFields()) {
        assertNull(field.getOrdinal());
        assertTrue(field.getValue() instanceof FudgeFieldContainer);
        LiveDataSpecification lsdi = LiveDataSpecification.fromFudgeMsg((FudgeFieldContainer) field.getValue());
        assertTrue(lsdi.equals(spec1) || lsdi.equals(spec2));
      }
    }
  }

}
