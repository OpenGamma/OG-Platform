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
import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
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
  private final FudgeRequestSender _requestSender;
  private final FudgeContext _fudgeContext;
  
  public DistributedEntitlementChecker(FudgeRequestSender requestSender) {
    this(requestSender, new FudgeContext());
  }
  
  public DistributedEntitlementChecker(FudgeRequestSender requestSender, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(requestSender, "Request Sender");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _requestSender = requestSender;
    _fudgeContext = fudgeContext;
  }

  @Override
  public boolean isEntitled(String userName,
      LiveDataSpecification fullyQualifiedSpecification) {
    s_logger.info("Sending message to qualify {} on {}", userName, fullyQualifiedSpecification);
    FudgeFieldContainer requestMessage = composeRequestMessage(userName, fullyQualifiedSpecification);
    final AtomicBoolean responseReceived = new AtomicBoolean(false);
    final AtomicBoolean isEntitled = new AtomicBoolean(false);
    _requestSender.sendRequest(requestMessage, new FudgeMessageReceiver() {
      
      @Override
      public void messageReceived(FudgeContext fudgeContext,
          FudgeMsgEnvelope msgEnvelope) {
        
        FudgeFieldContainer msg = msgEnvelope.getMessage();
        EntitlementResponse response = EntitlementResponse.fromFudgeMsg(msg);
        isEntitled.set(response.getIsEntitled());
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
    EntitlementRequest request = new EntitlementRequest(userName, new LiveDataSpecificationImpl(fullyQualifiedSpecification));
    return request.toFudgeMsg(new FudgeSerializationContext(_fudgeContext));
  }

}
