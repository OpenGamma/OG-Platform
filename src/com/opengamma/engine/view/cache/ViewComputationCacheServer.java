/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * Composite server class for dispatching calls to a {@link IdentifierMapServer} and 
 * {@link BinaryDataStoreServer} within the same JVM.
 */
public class ViewComputationCacheServer implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationCacheServer.class);

  private final IdentifierMapServer _identifierMap;
  private final BinaryDataStoreServer _binaryDataStore;

  public ViewComputationCacheServer(final IdentifierMapServer identifierMap, final BinaryDataStoreServer binaryDataStore) {
    _identifierMap = identifierMap;
    _binaryDataStore = binaryDataStore;
  }

  public ViewComputationCacheServer(final DefaultViewComputationCacheSource cacheSource) {
    this(new IdentifierMapServer(cacheSource.getIdentifierMap()), new BinaryDataStoreServer(cacheSource));
  }

  protected IdentifierMapServer getIdentifierMap() {
    return _identifierMap;
  }

  protected BinaryDataStoreServer getBinaryDataStore() {
    return _binaryDataStore;
  }

  /**
   * Handles the message.
   * 
   * @param message the message
   * @return the response, not {@code null}
   */
  protected CacheMessage handleCacheMessage(final CacheMessage message) {
    CacheMessage response = null;
    if (message instanceof BinaryDataStoreRequest) {
      response = getBinaryDataStore().handleBinaryDataStoreRequest((BinaryDataStoreRequest) message);
    } else if (message instanceof IdentifierMapRequest) {
      response = getIdentifierMap().handleIdentifierMapRequest((IdentifierMapRequest) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
    if (response == null) {
      response = new CacheMessage();
    }
    return response;
  }

  @Override
  public FudgeFieldContainer requestReceived(final FudgeDeserializationContext context, final FudgeMsgEnvelope requestEnvelope) {
    final CacheMessage request = context.fudgeMsgToObject(CacheMessage.class, requestEnvelope.getMessage());
    final FudgeContext fudgeContext = context.getFudgeContext();
    final CacheMessage response = handleCacheMessage(request);
    response.setCorrelationId(request.getCorrelationId());
    final FudgeSerializationContext ctx = new FudgeSerializationContext(fudgeContext);
    final MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
    // We have only one response type for each request, so don't really need the headers
    // FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), CacheMessage.class);
    return responseMsg;
  }

}
