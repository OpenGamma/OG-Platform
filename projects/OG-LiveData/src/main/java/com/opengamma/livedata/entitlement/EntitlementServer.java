/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.ArrayList;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
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
 * A server managing entitlements.
 * <p>
 * This receives {@link EntitlementRequest} requests, passing them onto a delegate
 * {@link LiveDataEntitlementChecker}, and returning {@link EntitlementResponseMsg} responses.
 */
public class EntitlementServer implements FudgeRequestReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EntitlementServer.class);

  /**
   * The underlying implementation.
   */
  private final LiveDataEntitlementChecker _delegate;

  /**
   * Creates an instance wrapping an underlying checker.
   * 
   * @param underlying  the underlying checker, not null
   */
  public EntitlementServer(LiveDataEntitlementChecker underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _delegate = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  @Transactional
  public FudgeMsg requestReceived(FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
    FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
    EntitlementRequest entitlementRequest = EntitlementRequest.fromFudgeMsg(deserializer, requestFudgeMsg);
    s_logger.debug("Received entitlement request {}", entitlementRequest);
    
    Map<LiveDataSpecification, Boolean> isEntitledMap = _delegate.isEntitled(entitlementRequest.getUser(), entitlementRequest.getLiveDataSpecifications());
    
    ArrayList<EntitlementResponse> responses = new ArrayList<EntitlementResponse>();
    for (LiveDataSpecification spec : entitlementRequest.getLiveDataSpecifications()) {
      boolean isEntitled = isEntitledMap.get(spec);
      EntitlementResponse response = isEntitled ?
          new EntitlementResponse(spec, true) :
          new EntitlementResponse(spec, false, entitlementRequest.getUser() + " is not entitled to " + spec);
      responses.add(response);
    }
    EntitlementResponseMsg response = new EntitlementResponseMsg(responses);
    return response.toFudgeMsg(new FudgeSerializer(deserializer.getFudgeContext()));
  }

}
