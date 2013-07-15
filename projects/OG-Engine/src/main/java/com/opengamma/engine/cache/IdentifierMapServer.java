/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.msg.CacheMessage;
import com.opengamma.engine.cache.msg.CacheMessageVisitor;
import com.opengamma.engine.cache.msg.IdentifierLookupRequest;
import com.opengamma.engine.cache.msg.IdentifierLookupResponse;
import com.opengamma.engine.cache.msg.SpecificationLookupRequest;
import com.opengamma.engine.cache.msg.SpecificationLookupResponse;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * Server for a {@link RemoteIdentifierMap}.
 */
public class IdentifierMapServer extends CacheMessageVisitor implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(IdentifierMapServer.class);

  private final IdentifierMap _underlying;

  public IdentifierMapServer(final IdentifierMap underlying) {
    _underlying = underlying;
  }

  protected IdentifierMap getUnderlying() {
    return _underlying;
  }

  @Override
  protected IdentifierLookupResponse visitIdentifierLookupRequest(final IdentifierLookupRequest request) {
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

  @Override
  protected SpecificationLookupResponse visitSpecificationLookupRequest(final SpecificationLookupRequest request) {
    final LongList identifiers = new AbstractLongList() {

      private List<Long> _raw = request.getIdentifier();

      @Override
      public long getLong(int index) {
        return _raw.get(index);
      }

      @Override
      public int size() {
        return _raw.size();
      }

    };
    final Collection<ValueSpecification> specifications;
    if (identifiers.size() == 1) {
      specifications = Collections.singleton(getUnderlying().getValueSpecification(identifiers.getLong(0)));
    } else {
      final Long2ObjectMap<ValueSpecification> specificationMap = getUnderlying().getValueSpecifications(identifiers);
      specifications = new ArrayList<ValueSpecification>(specificationMap.size());
      for (Long identifier : identifiers) {
        specifications.add(specificationMap.get(identifier));
      }
    }
    final SpecificationLookupResponse response = new SpecificationLookupResponse(specifications);
    return response;
  }

  @Override
  public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
    final CacheMessage request = deserializer.fudgeMsgToObject(CacheMessage.class, requestEnvelope.getMessage());
    final FudgeContext fudgeContext = deserializer.getFudgeContext();
    CacheMessage response = request.accept(this);
    if (response == null) {
      response = new CacheMessage();
    }
    response.setCorrelationId(request.getCorrelationId());
    final FudgeSerializer ctx = new FudgeSerializer(fudgeContext);
    final MutableFudgeMsg responseMsg = ctx.objectToFudgeMsg(response);
    // We have only one response for each request type, so don't need the headers
    // FudgeSerializer.addClassHeader(responseMsg, response.getClass(), IdentifierMapResponse.class);
    return responseMsg;
  }

  @Override
  protected <T extends CacheMessage> T visitUnexpectedMessage(final CacheMessage message) {
    s_logger.warn("Unexpected message {}", message);
    return null;
  }

}
