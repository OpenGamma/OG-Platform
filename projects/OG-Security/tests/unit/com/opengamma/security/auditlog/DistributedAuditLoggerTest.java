/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Test;

import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.CollectingByteArrayMessageSender;

/**
 * 
 *
 * @author pietari
 */
public class DistributedAuditLoggerTest {
  
  private static final FudgeContext s_fudgeContext = new FudgeContext ();
  
  @Test
  public void testClientServerAuditLogging() {
    
    CollectingByteArrayMessageSender msgStore = new CollectingByteArrayMessageSender();
    assertEquals(0, msgStore.getMessages().size());
    
    DistributedAuditLogger client = new DistributedAuditLogger("testoriginatingsystem", new ByteArrayFudgeMessageSender(msgStore));
    client.log("lisa", "testobject", "testop", "testdescription", true);
    assertEquals(1, msgStore.getMessages().size());
    
    FudgeMsgEnvelope fudgeMsgEnvelope = s_fudgeContext.deserialize(msgStore.getMessages().get(0));
    
    InMemoryAuditLogger memoryAuditLogger = new InMemoryAuditLogger();
    assertEquals(0, memoryAuditLogger.getMessages().size());
    
    DistributedAuditLoggerServer server = new DistributedAuditLoggerServer(memoryAuditLogger);
    server.messageReceived(s_fudgeContext, fudgeMsgEnvelope);
    assertEquals(1, memoryAuditLogger.getMessages().size());
    
    AuditLogEntry entry = memoryAuditLogger.getMessages().get(0);
    assertEquals("lisa", entry.getUser());
    assertEquals("testoriginatingsystem", entry.getOriginatingSystem());
    assertEquals("testobject", entry.getObject());
    assertEquals("testop", entry.getOperation());
    assertEquals("testdescription", entry.getDescription());
    assertTrue(entry.isSuccess());
    
    List<FudgeMsgEnvelope> msgEnvelopes = new ArrayList<FudgeMsgEnvelope>();
    msgEnvelopes.add(fudgeMsgEnvelope);
    msgEnvelopes.add(fudgeMsgEnvelope);
    msgEnvelopes.add(fudgeMsgEnvelope);
    server.messagesReceived(s_fudgeContext, msgEnvelopes);
    assertEquals(4, memoryAuditLogger.getMessages().size()); // 1 from messageReceived() + 3 from messagesReceived()
  }

}
