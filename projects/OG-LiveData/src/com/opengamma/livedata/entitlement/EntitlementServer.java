/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.ArrayList;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.EntitlementRequest;
import com.opengamma.livedata.msg.EntitlementResponse;
import com.opengamma.livedata.msg.EntitlementResponseMsg;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives {@link EntitlementRequests EntitlementRequest}, passes them onto a delegate
 * {@link LiveDataEntitlementChecker}, and returns {@link EntitlementResponseMsgs EntitlementResponseMsg}.
 */
public class EntitlementServer implements FudgeRequestReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(EntitlementServer.class);
  private final LiveDataEntitlementChecker _delegate;
  
  public EntitlementServer(LiveDataEntitlementChecker delegate) {
    ArgumentChecker.notNull(delegate, "Delegate entitlement checker");
    _delegate = delegate;
  }
  
  @Override
  @Transactional
  public FudgeFieldContainer requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    FudgeFieldContainer requestFudgeMsg = requestEnvelope.getMessage();
    EntitlementRequest entitlementRequest = EntitlementRequest.fromFudgeMsg(context, requestFudgeMsg);
    s_logger.debug("Received entitlement request {}", entitlementRequest);
    
    ArrayList<EntitlementResponse> responses = new ArrayList<EntitlementResponse>();
    for (LiveDataSpecification spec : entitlementRequest.getLiveDataSpecifications()) {
      
      boolean isEntitled = _delegate.isEntitled(entitlementRequest.getUser(), spec);
      
      EntitlementResponse response;
      if (isEntitled) {
        response = new EntitlementResponse(spec, true);
      } else {
        response = new EntitlementResponse(spec, false, entitlementRequest.getUser() + " is not entitled to " + spec);
      }
      responses.add(response);
    }
    
    EntitlementResponseMsg response = new EntitlementResponseMsg(responses);
    FudgeFieldContainer responseFudgeMsg = response.toFudgeMsg(new FudgeSerializationContext(context.getFudgeContext()));
    return responseFudgeMsg;
  }
  
}
