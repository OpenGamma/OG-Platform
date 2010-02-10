/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.EntitlementRequest;
import com.opengamma.livedata.EntitlementResponse;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Uses the OpenGamma transport system to enable distributed request/response
 * for entitlement checking.
 *
 * @author kirk
 */
public class DistributedEntitlementChecker implements
    LiveDataEntitlementChecker {
  public static final long TIMEOUT_MS = 5 * 60 * 1000l;
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedEntitlementChecker.class);
  private final ByteArrayRequestSender _requestSender;
  private final FudgeContext _fudgeContext;
  
  public DistributedEntitlementChecker(ByteArrayRequestSender requestSender) {
    this(requestSender, new FudgeContext());
  }
  
  public DistributedEntitlementChecker(ByteArrayRequestSender requestSender, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(requestSender, "Request Sender");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _requestSender = requestSender;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the requestSender
   */
  public ByteArrayRequestSender getRequestSender() {
    return _requestSender;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public boolean isEntitled(String userName,
      LiveDataSpecification fullyQualifiedSpecification) {
    s_logger.info("Sending message to qualify {} on {}", userName, fullyQualifiedSpecification);
    FudgeFieldContainer requestMessage = composeRequestMessage(userName, fullyQualifiedSpecification);
    byte[] requestBytes = getFudgeContext().toByteArray(requestMessage);
    final AtomicBoolean responseReceived = new AtomicBoolean(false);
    final AtomicBoolean isEntitled = new AtomicBoolean(false);
    getRequestSender().sendRequest(requestBytes, new ByteArrayMessageReceiver() {
      @Override
      public void messageReceived(byte[] message) {
        isEntitled.set(parseResponseMessage(message));
        responseReceived.set(true);
      }
    });
    long start = System.currentTimeMillis();
    while(!responseReceived.get()) {
      try {
        Thread.sleep(100l);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
      if((System.currentTimeMillis() - start) >= TIMEOUT_MS) {
        throw new OpenGammaRuntimeException("Timeout. Waited for entitlement response for " + TIMEOUT_MS + " with no response.");
      }
    }
    
    return isEntitled.get();
  }

  /**
   * @param userName
   * @param fullyQualifiedSpecification
   * @return
   */
  protected FudgeFieldContainer composeRequestMessage(String userName,
      LiveDataSpecification fullyQualifiedSpecification) {
    EntitlementRequest request = new EntitlementRequest(userName, fullyQualifiedSpecification);
    return request.toFudgeMsg(new FudgeSerializationContext(_fudgeContext));
  }

  protected boolean parseResponseMessage(byte[] message) {
    FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(message);
    if(msgEnvelope == null) {
      s_logger.warn("Recieved response message with no envelope. Not allowing access.");
      return false;
    }
    FudgeFieldContainer msg = msgEnvelope.getMessage();
    EntitlementResponse response = EntitlementResponse.fromFudgeMsg(msg);
    return response.getIsEntitled();
  }
}
