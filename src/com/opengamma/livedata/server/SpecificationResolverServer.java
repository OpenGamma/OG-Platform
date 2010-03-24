/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.ResolveRequest;
import com.opengamma.livedata.ResolveResponse;
import com.opengamma.livedata.client.LiveDataSpecificationResolver;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives <code>ResolveRequests</code>, passes them onto a delegate <code>LiveDataSpecificationResolver</code>,
 * and returns <code>ResolveResponses</code>. 
 *
 * @author pietari
 */
public class SpecificationResolverServer implements FudgeRequestReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SpecificationResolverServer.class);
  private final LiveDataSpecificationResolver _delegate;
  
  public SpecificationResolverServer(LiveDataSpecificationResolver delegate) {
    ArgumentChecker.checkNotNull(delegate, "Delegate specification resolver");
    _delegate = delegate;
  }
  
  @Override
  public FudgeFieldContainer requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    FudgeFieldContainer requestFudgeMsg = requestEnvelope.getMessage();
    ResolveRequest resolveRequest = ResolveRequest.fromFudgeMsg(context, requestFudgeMsg);
    s_logger.debug("Received resolve request for {}", resolveRequest.getRequestedSpecification());
    
    LiveDataSpecification resolvedSpec = _delegate.resolve(resolveRequest.getRequestedSpecification());
    ResolveResponse response = new ResolveResponse(new LiveDataSpecification(resolvedSpec));
    FudgeFieldContainer responseFudgeMsg = response.toFudgeMsg(new FudgeSerializationContext(context.getFudgeContext()));
    return responseFudgeMsg;
  }
  
}
