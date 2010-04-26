/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.msg.EntitlementRequest;
import com.opengamma.livedata.msg.EntitlementResponse;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives <code>EntitlementRequests</code>, passes them onto a delegate <code>LiveDataEntitlementChecker</code>,
 * and returns <code>EntitlementResponses</code>. 
 *
 * @author pietari
 */
public class EntitlementServer implements FudgeRequestReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(EntitlementServer.class);
  private final LiveDataEntitlementChecker _delegate;
  private final DistributionSpecificationResolver _distributionSpecResolver;
  
  public EntitlementServer(LiveDataEntitlementChecker delegate,
      DistributionSpecificationResolver distributionSpecResolver) {
    ArgumentChecker.notNull(delegate, "Delegate entitlement checker");
    ArgumentChecker.notNull(distributionSpecResolver, "Distribution spec resolver");
    _delegate = delegate;
    _distributionSpecResolver = distributionSpecResolver;
  }
  
  @Override
  public FudgeFieldContainer requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    FudgeFieldContainer requestFudgeMsg = requestEnvelope.getMessage();
    EntitlementRequest entitlementRequest = EntitlementRequest.fromFudgeMsg(context, requestFudgeMsg);
    s_logger.debug("Received entitlement request from {} for {}", entitlementRequest.getUserName(), entitlementRequest.getLiveDataSpecification());
    
    DistributionSpecification distSpec = _distributionSpecResolver.getDistributionSpecification(entitlementRequest.getLiveDataSpecification());
    
    boolean isEntitled = _delegate.isEntitled(entitlementRequest.getUserName(), distSpec);
    
    EntitlementResponse response = new EntitlementResponse(isEntitled);
    FudgeFieldContainer responseFudgeMsg = response.toFudgeMsg(new FudgeSerializationContext(context.getFudgeContext()));
    return responseFudgeMsg;
  }
  
}
