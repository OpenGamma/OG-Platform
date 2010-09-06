/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.DefaultViewComputationCacheSource.ReleaseCachesCallback;
import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.BinaryDataStoreResponse;
import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.DeleteRequest;
import com.opengamma.engine.view.cache.msg.GetRequest;
import com.opengamma.engine.view.cache.msg.GetResponse;
import com.opengamma.engine.view.cache.msg.PutRequest;
import com.opengamma.engine.view.cache.msg.ReleaseCacheMessage;
import com.opengamma.engine.view.cache.msg.SlaveChannelMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Server for {@link RemoteBinaryDataStore} clients created by a {@link RemoteBinaryDataStoreFactory}.
 * The underlying is the shared data store component of a {@link DefaultViewComputationCache}.
 */
public class BinaryDataStoreServer implements FudgeConnectionReceiver, ReleaseCachesCallback, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(BinaryDataStoreServer.class);
  private static final byte[] EMPTY_ARRAY = new byte[0];

  private final ExecutorService _executorService = Executors.newCachedThreadPool();
  private final DefaultViewComputationCacheSource _underlying;
  private final Map<FudgeConnection, Object> _connections = new ConcurrentHashMap<FudgeConnection, Object>();

  public BinaryDataStoreServer(final DefaultViewComputationCacheSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    underlying.setReleaseCachesCallback(this);
  }

  protected DefaultViewComputationCacheSource getUnderlying() {
    return _underlying;
  }

  protected Map<FudgeConnection, Object> getConnections() {
    return _connections;
  }

  protected void handleDelete(final DeleteRequest request) {
    getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp()).getSharedDataStore().delete();
  }

  protected GetResponse handleGet(final GetRequest request) {
    final List<Long> identifiers = request.getIdentifier();
    final Collection<byte[]> response;
    if (identifiers.size() == 1) {
      byte[] data = getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp()).getSharedDataStore().get(identifiers.get(0));
      if (data == null) {
        data = EMPTY_ARRAY;
      }
      response = Collections.singleton(data);
    } else {
      response = new ArrayList<byte[]>(identifiers.size());
      final Map<Long, byte[]> data = getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp()).getSharedDataStore().get(identifiers);
      for (Long identifier : identifiers) {
        byte[] value = data.get(identifier);
        if (value == null) {
          value = EMPTY_ARRAY;
        }
        response.add(value);
      }
    }
    return new GetResponse(response);
  }

  protected void handlePut(final PutRequest request) {
    final List<Long> identifiers = request.getIdentifier();
    final List<byte[]> data = request.getData();
    if (identifiers.size() == 1) {
      getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp()).getSharedDataStore().put(identifiers.get(0), data.get(0));
    } else {
      final Map<Long, byte[]> map = new HashMap<Long, byte[]>();
      final Iterator<Long> i = identifiers.iterator();
      final Iterator<byte[]> j = data.iterator();
      while (i.hasNext()) {
        map.put(i.next(), j.next());
      }
      getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp()).getSharedDataStore().put(map);
    }
  }

  protected void handleSlaveChannel(final SlaveChannelMessage request, final FudgeConnection connection) {
    getConnections().remove(connection);
  }

  /**
   * Handles the request. 
   * 
   * @param request the request
   * @param connection the connection
   * @return a response, or {@code null} for an asynchronous message
   */
  protected BinaryDataStoreResponse handleBinaryDataStoreRequest(final BinaryDataStoreRequest request, final FudgeConnection connection) {
    BinaryDataStoreResponse response = null;
    if (request instanceof GetRequest) {
      response = handleGet((GetRequest) request);
    } else if (request instanceof PutRequest) {
      handlePut((PutRequest) request);
    } else if (request instanceof DeleteRequest) {
      handleDelete((DeleteRequest) request);
    } else if (request instanceof SlaveChannelMessage) {
      handleSlaveChannel((SlaveChannelMessage) request, connection);
    } else {
      s_logger.warn("Unexpected message - {}", request);
    }
    if (response == null) {
      if (request.getCorrelationId() != null) {
        response = new BinaryDataStoreResponse();
      }
    }
    return response;
  }

  @Override
  public void onReleaseCaches(final String viewName, final long timestamp) {
    final MutableFudgeFieldContainer message = getUnderlying().getFudgeContext().newMessage();
    new ReleaseCacheMessage(viewName, timestamp).toFudgeMsg(getUnderlying().getFudgeContext(), message);
    FudgeSerializationContext.addClassHeader(message, ReleaseCacheMessage.class, CacheMessage.class);
    for (Map.Entry<FudgeConnection, Object> connectionEntry : getConnections().entrySet()) {
      final FudgeConnection connection = connectionEntry.getKey();
      s_logger.debug("onReleaseCaches - {}/{} on {}", new Object[] {viewName, timestamp, connection});
      _executorService.execute(new Runnable() {

        @Override
        public void run() {
          s_logger.debug("Releasing {}/{} on connection {}", new Object[] {viewName, timestamp, connection});
          connection.getFudgeMessageSender().send(message);
        }

      });
    }
  }

  protected void handleMessage(final FudgeConnection connection, final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(fudgeContext);
    final BinaryDataStoreRequest request = dctx.fudgeMsgToObject(BinaryDataStoreRequest.class, msgEnvelope.getMessage());
    final BinaryDataStoreResponse response = handleBinaryDataStoreRequest(request, connection);
    if (response != null) {
      response.setCorrelationId(request.getCorrelationId());
      final FudgeSerializationContext sctx = new FudgeSerializationContext(fudgeContext);
      final MutableFudgeFieldContainer responseMsg = sctx.objectToFudgeMsg(response);
      // We have only one response for each request type, so don't really need the headers
      // FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), BinaryDataStoreResponse.class);
      connection.getFudgeMessageSender().send(responseMsg);
    }
  }

  protected void onNewConnection(final FudgeConnection connection) {
    getConnections().put(connection, EMPTY_ARRAY);
  }

  protected void onDroppedConnection(final FudgeConnection connection) {
    getConnections().remove(connection);
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    s_logger.info("New connection from {}", connection);
    connection.setConnectionStateListener(this);
    onNewConnection(connection);
    handleMessage(connection, fudgeContext, message);
    connection.setFudgeMessageReceiver(new FudgeMessageReceiver() {

      @Override
      public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
        handleMessage(connection, fudgeContext, msgEnvelope);
      }

    });
  }

  @Override
  public void connectionFailed(final FudgeConnection connection, final Exception cause) {
    s_logger.info("Dropped connection from {}", connection);
    onDroppedConnection(connection);
  }

  @Override
  public void connectionReset(final FudgeConnection connection) {
    // No action
  }

}
