/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.EntitlementRequest;
import com.opengamma.livedata.msg.EntitlementResponse;
import com.opengamma.livedata.msg.EntitlementResponseMsg;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Checks entitlements against a LiveData server by sending the server a Fudge message.
 */
public class DistributedEntitlementChecker {
  
  /**
   * If no response from server is received within this period of time, throw exception
   */
  public static final long TIMEOUT_MS = 5000;
  
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedEntitlementChecker.class);
  private final FudgeRequestSender _requestSender;
  private final FudgeContext _fudgeContext;
  
  public DistributedEntitlementChecker(FudgeRequestSender requestSender) {
    this(requestSender, OpenGammaFudgeContext.getInstance());
  }
  
  public DistributedEntitlementChecker(FudgeRequestSender requestSender, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(requestSender, "Request Sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _requestSender = requestSender;
    _fudgeContext = fudgeContext;
  }

  @SuppressWarnings("unused")
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user,
      Collection<LiveDataSpecification> specifications) {
    s_logger.info("Checking entitlements by {} to {}", user, specifications);

    // The entitlement check completely and utterly screws up the market data
    // server for Activ, wiping out valid resolved ids. As the result of the
    // entitlement check is currently ignored, we'll just avoid doing it at all!
    if (true) {
      return ImmutableMap.of();
    }

    final Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();

    if (specifications == null || specifications.size() == 0) {
      // Nothing to check
      return returnValue;
    }

    FudgeMsg requestMessage = composeRequestMessage(user, specifications);
    
    final CountDownLatch latch = new CountDownLatch(1);
    
    _requestSender.sendRequest(requestMessage, new FudgeMessageReceiver() {
      
      @Override
      public void messageReceived(FudgeContext fudgeContext,
          FudgeMsgEnvelope msgEnvelope) {
        
        FudgeMsg msg = msgEnvelope.getMessage();
        EntitlementResponseMsg responseMsg = EntitlementResponseMsg.fromFudgeMsg(new FudgeDeserializer(fudgeContext), msg);
        for (EntitlementResponse response : responseMsg.getResponses()) {
          returnValue.put(response.getLiveDataSpecification(), response.getIsEntitled());
        }
        latch.countDown();
      }
    });
    
    boolean success;
    try {
      success = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    
    if (!success) {
      throw new OpenGammaRuntimeException("Timeout. Waited for entitlement response for " + TIMEOUT_MS + " with no response.");
    }
    
    s_logger.info("Got entitlement response {}", returnValue);
    return returnValue;
  }
  
  public boolean isEntitled(UserPrincipal user,
      LiveDataSpecification specification) {
    Map<LiveDataSpecification, Boolean> entitlements = isEntitled(user, Collections.singleton(specification));
    return entitlements.get(specification);
  }

  private FudgeMsg composeRequestMessage(UserPrincipal user,
      Collection<LiveDataSpecification> specifications) {
    EntitlementRequest request = new EntitlementRequest(user, specifications);
    return request.toFudgeMsg(new FudgeSerializer(_fudgeContext));
  }

}
