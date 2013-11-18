/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the various Fudge-specific conduit forms.
 */
@Test(groups = TestGroup.INTEGRATION)
public class FudgeConduitTest {

  public void oneWayTest() {
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);
    
    MutableFudgeMsg msg = context.newMessage();
    msg.add("Foo", "Bar");
    msg.add("Number Problems", 99);
    
    fudgeSender.send(msg);
    
    List<FudgeMsgEnvelope> receivedMessages = collectingReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(2, receivedMsg.getNumFields());
    assertEquals("Bar", receivedMsg.getString("Foo"));
    assertEquals(new Integer(99), receivedMsg.getInt("Number Problems"));
  }
  
  public void oneWayTestWithEncryption() {
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver, OpenGammaFudgeContext.getInstance(), true);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context, true);
    
    MutableFudgeMsg msg = context.newMessage();
    msg.add("Foo", "Bar");
    msg.add("Number Problems", 99);
    
    fudgeSender.send(msg);
    
    List<FudgeMsgEnvelope> receivedMessages = collectingReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(2, receivedMsg.getNumFields());
    assertEquals("Bar", receivedMsg.getString("Foo"));
    assertEquals(new Integer(99), receivedMsg.getInt("Number Problems"));
  }
  
  public void requestResponseTest() {
    FudgeContext context = new FudgeContext();
    FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      @Override
      public FudgeMsg requestReceived(
          FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
        MutableFudgeMsg response = deserializer.getFudgeContext().newMessage();
        response.add("Killing", "In The Name Of");
        return response;
      }
    };
    
    FudgeRequestSender sender = InMemoryRequestConduit.create(requestReceiver);
    
    MutableFudgeMsg request = context.newMessage();
    request.add("Rage", "Against The Machine");
    
    CollectingFudgeMessageReceiver responseReceiver = new CollectingFudgeMessageReceiver();
    sender.sendRequest(request, responseReceiver);
    List<FudgeMsgEnvelope> receivedMessages = responseReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    
    FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(1, receivedMsg.getNumFields());
    assertEquals("In The Name Of", receivedMsg.getString("Killing"));
  }
}
