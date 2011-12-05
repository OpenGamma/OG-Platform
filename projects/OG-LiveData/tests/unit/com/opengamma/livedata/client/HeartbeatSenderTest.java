/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationFudgeBuilder;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.transport.CollectingByteArrayMessageSender;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
public class HeartbeatSenderTest {
  private Timer _timer = null;

  @BeforeMethod
  public void startTimer() {
    _timer = new Timer("HeartbeatSenderTest Timer");
  }
  
  @AfterMethod
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
        ExternalId.of("foo", "bar"));
    LiveDataSpecification spec2 = new LiveDataSpecification(
        "Test",
        ExternalId.of("foo", "baz"));
    valueDistributor.addListener(spec1, listener1);
    valueDistributor.addListener(spec2, listener1);
    
    @SuppressWarnings("unused")
    HeartbeatSender heartbeatSender = new HeartbeatSender(messageSender, valueDistributor, OpenGammaFudgeContext.getInstance(), _timer, 100l);
    // Wait 250ms to make sure we get two ticks.
    Thread.sleep(250l);
    
    List<byte[]> messages = messageSender.getMessages();
    assertTrue(messages.size() >= 2);
    
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    
    for (byte[] message : messages) {
      FudgeMsgEnvelope fudgeMsgEnvelope = fudgeContext.deserialize(message);
      FudgeMsg fudgeMsg = fudgeMsgEnvelope.getMessage();
      assertNotNull(fudgeMsg);
      assertEquals(2, fudgeMsg.getNumFields());
      for(FudgeField field : fudgeMsg.getAllFields()) {
        assertNull(field.getOrdinal());
        assertTrue(field.getValue() instanceof FudgeMsg);
        LiveDataSpecification lsdi = LiveDataSpecificationFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(fudgeContext), (FudgeMsg) field.getValue());
        assertTrue(lsdi.equals(spec1) || lsdi.equals(spec2));
      }
    }
  }

}
