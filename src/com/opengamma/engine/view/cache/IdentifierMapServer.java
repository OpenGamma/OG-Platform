/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.opengamma.engine.view.cache.msg.IdentifierLookupRequest;
import com.opengamma.engine.view.cache.msg.IdentifierLookupResponse;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.engine.view.cache.msg.IdentifierMapResponse;
import com.opengamma.engine.view.cache.msg.SpecificationLookupRequest;
import com.opengamma.engine.view.cache.msg.SpecificationLookupResponse;
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

  protected IdentifierLookupResponse handleIdentifierLookup(final IdentifierLookupRequest request) {
    final List<ValueSpecification> spec = request.getSpecification();
    final Collection<Long> identifiers;
    if (spec.size() == 1) {
      identifiers = Collections.singleton(getUnderlying().getIdentifier(spec.get(0)));
    } else {
      final Map<ValueSpecification, Long> identifierMap = getUnderlying().getIdentifiers(spec);
      identifiers = new ArrayList<Long>(identifierMap.size());
      for (ValueSpecification specEntry : spec) {
        identifiers.add(identifierMap.get(specEntry));
      }
    }
    final IdentifierLookupResponse response = new IdentifierLookupResponse(identifiers);
    return response;
  }

  protected SpecificationLookupResponse handleSpecificationLookup(final SpecificationLookupRequest request) {
    final List<Long> identifiers = request.getIdentifier();
    final Collection<ValueSpecification> specifications;
    if (identifiers.size() == 1) {
      specifications = Collections.singleton(getUnderlying().getValueSpecification(identifiers.get(0)));
    } else {
      final Map<Long, ValueSpecification> specificationMap = getUnderlying().getValueSpecifications(identifiers);
      specifications = new ArrayList<ValueSpecification>(specificationMap.size());
      for (Long identifier : identifiers) {
        specifications.add(specificationMap.get(identifier));
      }
    }
    final SpecificationLookupResponse response = new SpecificationLookupResponse(specifications);
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
    if (request instanceof IdentifierLookupRequest) {
      response = handleIdentifierLookup((IdentifierLookupRequest) request);
    } else if (request instanceof SpecificationLookupRequest) {
      response = handleSpecificationLookup((SpecificationLookupRequest) request);
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
