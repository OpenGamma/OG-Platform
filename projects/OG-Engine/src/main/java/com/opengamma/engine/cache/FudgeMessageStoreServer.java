/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.DefaultViewComputationCacheSource.MissingValueLoader;
import com.opengamma.engine.cache.DefaultViewComputationCacheSource.ReleaseCachesCallback;
import com.opengamma.engine.cache.msg.CacheMessage;
import com.opengamma.engine.cache.msg.CacheMessageVisitor;
import com.opengamma.engine.cache.msg.DeleteRequest;
import com.opengamma.engine.cache.msg.FindMessage;
import com.opengamma.engine.cache.msg.GetRequest;
import com.opengamma.engine.cache.msg.GetResponse;
import com.opengamma.engine.cache.msg.PutRequest;
import com.opengamma.engine.cache.msg.ReleaseCacheMessage;
import com.opengamma.engine.cache.msg.SlaveChannelMessage;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Server for {@link RemoteFudgeMessageStore} clients created by a {@link RemoteFudgeMessageStoreFactory}. The underlying is the shared data store component of a {@link DefaultViewComputationCache}.
 */
public class FudgeMessageStoreServer implements FudgeConnectionReceiver, ReleaseCachesCallback, MissingValueLoader, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeMessageStoreServer.class);

  private static class ValueSearch {

    private final ConcurrentMap<Long, CountDownLatch> _pending = new ConcurrentHashMap<Long, CountDownLatch>();
    private int _refCount = 1;

    public void incrementRefCount() {
      _refCount++;
    }

    public int decrementAndGetRefCount() {
      return --_refCount;
    }

    public void found(final Long identifier) {
      final CountDownLatch sync = _pending.remove(identifier);
      if (sync != null) {
        sync.countDown();
      }
    }

    public boolean waitFor(final Long identifier, final long timeout) throws InterruptedException {
      if (timeout <= 0) {
        return false;
      }
      CountDownLatch latch = new CountDownLatch(1);
      final CountDownLatch previousLatch = _pending.putIfAbsent(identifier, latch);
      if (previousLatch != null) {
        latch = previousLatch;
      }
      return latch.await(timeout, TimeUnit.MILLISECONDS);
    }

  }

  private static final ExecutorService s_executorService = NamedThreadPoolFactory.newCachedThreadPool("FudgeMessageStoreBroadcast", true);
  private final DefaultViewComputationCacheSource _underlying;
  private final Map<FudgeConnection, Object> _connections = new ConcurrentHashMap<FudgeConnection, Object>();
  private final Map<ViewComputationCacheKey, ValueSearch> _searching = new HashMap<ViewComputationCacheKey, ValueSearch>();

  private long _findValueTimeout = 5000L; // 5s default timeout

  public FudgeMessageStoreServer(final DefaultViewComputationCacheSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    underlying.setReleaseCachesCallback(this);
    underlying.setMissingValueLoader(this);
  }

  protected DefaultViewComputationCacheSource getUnderlying() {
    return _underlying;
  }

  protected Map<FudgeConnection, Object> getConnections() {
    return _connections;
  }

  /**
   * Asynchronously sends a message to all open connections.
   * 
   * @param message the message to send, not null.
   */
  protected void broadcast(final CacheMessage message) {
    final MutableFudgeMsg msg = getUnderlying().getFudgeContext().newMessage();
    message.toFudgeMsg(new FudgeSerializer(getUnderlying().getFudgeContext()), msg);
    FudgeSerializer.addClassHeader(msg, message.getClass(), CacheMessage.class);
    for (Map.Entry<FudgeConnection, Object> connectionEntry : getConnections().entrySet()) {
      final FudgeConnection connection = connectionEntry.getKey();
      s_executorService.execute(new Runnable() {
        @Override
        public void run() {
          connection.getFudgeMessageSender().send(msg);
        }
      });
    }
  }

  @Override
  public void onReleaseCaches(final UniqueId viewCycleId) {
    s_logger.debug("onReleaseCaches - {}", viewCycleId);
    broadcast(new ReleaseCacheMessage(viewCycleId));
  }

  public long getFindValueTimeout() {
    return _findValueTimeout;
  }

  /**
   * Set the timeout for any given value wait when retrieving data from the clients' private caches. Set to {@code 0} for no timeout.
   * 
   * @param findValueTimeout the timeout in milliseconds.
   */
  public void setFindValueTimeout(final long findValueTimeout) {
    _findValueTimeout = findValueTimeout;
  }

  @Override
  public FudgeMsg findMissingValue(final ViewComputationCacheKey cacheKey, final long identifier) {
    s_logger.debug("findMissing value {}", identifier);
    broadcast(new FindMessage(cacheKey.getViewCycleId(), cacheKey.getCalculationConfigurationName(), Collections.singleton(identifier)));
    // We're in the callback so we know the cache must exist
    final FudgeMessageStore store = getUnderlying().findCache(cacheKey).getSharedDataStore();
    FudgeMsg data = store.get(identifier);
    if (data == null) {
      final ValueSearch search = getOrCreateValueSearch(cacheKey);
      try {
        s_logger.debug("Waiting for missing value {} to appear", identifier);
        if (!search.waitFor(identifier, getFindValueTimeout())) {
          s_logger.warn("{}ms timeout exceeded waiting for value ID {}", getFindValueTimeout(), identifier);
          // don't try to avoid the store.get call as data may yet arrive
        }
      } catch (InterruptedException e) {
        s_logger.warn("Thread interrupted waiting for missing value response");
        // don't try to avoid the store.get call as data may yet arrive
      }
      data = store.get(identifier);
      releaseValueSearch(cacheKey, search);
    }
    if (data != null) {
      s_logger.debug("Value for {} found and transferred to shared data store", identifier);
    }
    return data;
  }

  @Override
  public Map<Long, FudgeMsg> findMissingValues(final ViewComputationCacheKey cache,
      final Collection<Long> identifiers) {
    s_logger.debug("findMissing values {}", identifiers);
    broadcast(new FindMessage(cache.getViewCycleId(), cache.getCalculationConfigurationName(), identifiers));
    final ValueSearch search = getOrCreateValueSearch(cache);
    // We're in the callback so we know the cache must exist
    final FudgeMessageStore store = getUnderlying().findCache(cache).getSharedDataStore();
    final Long[] identifierArray = new Long[identifiers.size()];
    int identifierCount = 0;
    for (Long identifier : identifiers) {
      identifierArray[identifierCount++] = identifier;
    }
    final Map<Long, FudgeMsg> map = new HashMap<Long, FudgeMsg>();
    try {
      while (identifierCount > 0) {
        final Long identifier = identifierArray[0];
        FudgeMsg data = store.get(identifier);
        if (data != null) {
          s_logger.debug("Value for {} found and transferred to shared data store", identifier);
          map.put(identifier, data);
          identifierArray[0] = identifierArray[--identifierCount];
          continue;
        }
        s_logger.debug("Waiting for missing value ID {} to appear (of {} remaining values)", identifier, identifierCount);
        if (!search.waitFor(identifier, getFindValueTimeout())) {
          s_logger.warn("{}ms timeout exceeded waiting for value ID {}", getFindValueTimeout(), identifier);
          for (int i = 0; i < identifierCount; i++) {
            data = store.get(identifierArray[i]);
            if (data != null) {
              s_logger.debug("Value for {} found and transferred to shared data store", identifierArray[i]);
              map.put(identifierArray[i], data);
            }
          }
          break;
        }
      }
    } catch (InterruptedException e) {
      s_logger.warn("Thread interrupted waiting for missing value response - {} outstanding", identifierCount);
    }
    releaseValueSearch(cache, search);
    return map;
  }

  protected synchronized ValueSearch getOrCreateValueSearch(final ViewComputationCacheKey key) {
    ValueSearch search = _searching.get(key);
    if (search == null) {
      search = new ValueSearch();
      _searching.put(key, search);
    } else {
      search.incrementRefCount();
    }
    return search;
  }

  protected synchronized void releaseValueSearch(final ViewComputationCacheKey key, final ValueSearch search) {
    if (search.decrementAndGetRefCount() == 0) {
      _searching.remove(key);
    }
  }

  protected synchronized ValueSearch getValueSearch(final ViewComputationCacheKey key) {
    return _searching.get(key);
  }

  private class MessageHandler extends CacheMessageVisitor implements FudgeMessageReceiver {

    private final FudgeConnection _connection;

    public MessageHandler(final FudgeConnection connection) {
      _connection = connection;
    }

    private FudgeConnection getConnection() {
      return _connection;
    }

    @Override
    protected <T extends CacheMessage> T visitUnexpectedMessage(final CacheMessage message) {
      s_logger.warn("Unexpected message {}", message);
      return null;
    }

    @Override
    protected CacheMessage visitDeleteRequest(final DeleteRequest request) {
      // [ENG-256] Remove/replace this. Propogate the overall "releaseCache" message only rather than the component "delete" operations.
      final DefaultViewComputationCache cache = getUnderlying().findCache(request.getViewCycleId(), request.getCalculationConfigurationName());
      if (cache != null) {
        cache.getSharedDataStore().delete();
      }
      return null;
    }

    @Override
    protected GetResponse visitGetRequest(final GetRequest request) {
      final List<Long> identifiers = request.getIdentifier();
      final Collection<FudgeMsg> response;
      final DefaultViewComputationCache cache = getUnderlying().findCache(request.getViewCycleId(), request.getCalculationConfigurationName());
      if (cache == null) {
        // Can happen if a node runs slowly, the job is retried elsewhere and the cycle completed while the original node is still generating traffic
        s_logger.warn("Get request on invalid cache - {}", request);
        response = Collections.singleton(FudgeContext.EMPTY_MESSAGE);
      } else {
        final FudgeMessageStore store = cache.getSharedDataStore();
        if (identifiers.size() == 1) {
          FudgeMsg data = store.get(identifiers.get(0));
          if (data == null) {
            data = FudgeContext.EMPTY_MESSAGE;
          }
          response = Collections.singleton(data);
        } else {
          response = new ArrayList<FudgeMsg>(identifiers.size());
          final Map<Long, FudgeMsg> data = store.get(identifiers);
          for (Long identifier : identifiers) {
            FudgeMsg value = data.get(identifier);
            if (value == null) {
              value = FudgeContext.EMPTY_MESSAGE;
            }
            response.add(value);
          }
        }
      }
      return new GetResponse(response);
    }

    @Override
    protected CacheMessage visitPutRequest(final PutRequest request) {
      final List<Long> identifiers = request.getIdentifier();
      final List<FudgeMsg> data = request.getData();
      final ViewComputationCacheKey key = new ViewComputationCacheKey(request.getViewCycleId(), request.getCalculationConfigurationName());
      // Review 2010-10-19 Andrew -- This causes cache creation. This is bad if messages were delayed and the cache has already been released.
      final FudgeMessageStore store = getUnderlying().getCache(key).getSharedDataStore();
      if (identifiers.size() == 1) {
        store.put(identifiers.get(0), data.get(0));
      } else {
        final Map<Long, FudgeMsg> map = new HashMap<Long, FudgeMsg>();
        final Iterator<Long> i = identifiers.iterator();
        final Iterator<FudgeMsg> j = data.iterator();
        while (i.hasNext()) {
          map.put(i.next(), j.next());
        }
        store.put(map);
      }
      final ValueSearch searching = getValueSearch(key);
      if (searching != null) {
        for (Long identifier : identifiers) {
          searching.found(identifier);
        }
      }
      return null;
    }

    @Override
    protected CacheMessage visitSlaveChannelMessage(final SlaveChannelMessage message) {
      getConnections().remove(getConnection());
      return null;
    }

    @Override
    public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
      final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
      final CacheMessage request = deserializer.fudgeMsgToObject(CacheMessage.class, msgEnvelope.getMessage());
      CacheMessage response = request.accept(this);
      if (response == null) {
        if (request.getCorrelationId() != null) {
          response = new CacheMessage();
        }
      }
      if (response != null) {
        response.setCorrelationId(request.getCorrelationId());
        final FudgeSerializer sctx = new FudgeSerializer(fudgeContext);
        final MutableFudgeMsg responseMsg = sctx.objectToFudgeMsg(response);
        // We have only one response for each request type, so don't really need the headers
        // FudgeSerializer.addClassHeader(responseMsg, response.getClass(), BinaryDataStoreResponse.class);
        getConnection().getFudgeMessageSender().send(responseMsg);
      }
    }

  };

  protected MessageHandler onNewConnection(final FudgeConnection connection) {
    getConnections().put(connection, FudgeContext.EMPTY_MESSAGE);
    return new MessageHandler(connection);
  }

  protected void onDroppedConnection(final FudgeConnection connection) {
    getConnections().remove(connection);
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    s_logger.info("New connection from {}", connection);
    connection.setConnectionStateListener(this);
    final MessageHandler handler = onNewConnection(connection);
    handler.messageReceived(fudgeContext, message);
    connection.setFudgeMessageReceiver(handler);
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
