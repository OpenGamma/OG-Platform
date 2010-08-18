/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.engine.view.cache.msg.IdentifierMapResponse;
import com.opengamma.engine.view.cache.msg.LookupRequest;
import com.opengamma.engine.view.cache.msg.LookupResponse;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * Server for a {@link RemoteIdentifierMap}.
 */
public class IdentifierMapServer implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(IdentifierMapServer.class);

  private final IdentifierMap _underlying;

  public IdentifierMapServer(final IdentifierMap underlying) {
    _underlying = underlying;
  }

  protected IdentifierMap getUnderlying() {
    return _underlying;
  }

  protected LookupResponse handleLookup(final LookupRequest request) {
    final List<ValueSpecification> spec = request.getSpecification();
    final long[] identifiers;
    if (spec.size() == 1) {
      identifiers = new long[] {getUnderlying().getIdentifier(spec.get(0))};
    } else {
      identifiers = new long[spec.size()];
      final Map<ValueSpecification, Long> identifierMap = getUnderlying().getIdentifiers(spec);
      int i = 0;
      for (ValueSpecification valueSpec : spec) {
        identifiers[i++] = identifierMap.get(valueSpec);
      }
    }
    final LookupResponse response = new LookupResponse(identifiers);
    return response;
  }

  /**
   * Handles the request.
   * 
   * @param request the request
   * @return the response, not {@code null}
   */
  protected IdentifierMapResponse handleIdentifierMapRequest(final IdentifierMapRequest request) {
    IdentifierMapResponse response = null;
    if (request instanceof LookupRequest) {
      response = handleLookup((LookupRequest) request);
    } else {
      s_logger.warn("Unexpected message {}", request);
    }
    if (response == null) {
      response = new IdentifierMapResponse();
    }
    return response;
  }

  @Override
  public FudgeFieldContainer requestReceived(final FudgeDeserializationContext context, final FudgeMsgEnvelope requestEnvelope) {
    final IdentifierMapRequest request = context.fudgeMsgToObject(IdentifierMapRequest.class, requestEnvelope.getMessage());
    final FudgeContext fudgeContext = context.getFudgeContext();
    final IdentifierMapResponse response = handleIdentifierMapRequest(request);
    response.setCorrelationId(request.getCorrelationId());
    final FudgeSerializationContext ctx = new FudgeSerializationContext(fudgeContext);
    final MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
    // We have only one response for each request type, so don't need the headers
    // FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), IdentifierMapResponse.class);
    return responseMsg;
  }

}
