/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteCacheClient implements FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheClient.class);

  private static final long DEFAULT_TIMEOUT_MILLISECONDS = 30L * 1000L;

  private final ConcurrentMap<ValueSpecification, Long> _specificationIds = new ConcurrentHashMap<ValueSpecification, Long>();
  private final FudgeRequestSender _requestSender;
  private final ConcurrentMap<RemoteCacheMessage, OperationResult<?>> _activeOperations = new ConcurrentHashMap<RemoteCacheMessage, OperationResult<?>>();
  private final ConcurrentMap<Long, OperationResult<?>> _pendingOperations = new ConcurrentHashMap<Long, OperationResult<?>>();
  private final AtomicLong _correlationId = new AtomicLong(1L);

  private long _timeoutMilliseconds = DEFAULT_TIMEOUT_MILLISECONDS;

  /**
   * 
   */
  public final class OperationResult<T> {
    private final RemoteCacheMessage _operation;
    private final CountDownLatch _latch;
    private T _result;

    private OperationResult(final RemoteCacheMessage operation) {
      _operation = operation;
      _latch = new CountDownLatch(1);
    }

    public T getResult() {
      return _result;
    }

    public void release() {
      getActiveOperations().remove(_operation);
    }

    private boolean await() {
      try {
        return _latch.await(getTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        s_logger.warn("Thread interrupted {}", e);
        return false;
      }
    }
  }

  public RemoteCacheClient(FudgeRequestSender requestSender) {
    _requestSender = requestSender;
  }

  public void setTimeoutMilliseconds(final long timeoutMilliseconds) {
    _timeoutMilliseconds = timeoutMilliseconds;
  }

  public long getTimeoutMilliseconds() {
    return _timeoutMilliseconds;
  }

  protected FudgeRequestSender getRequestSender() {
    return _requestSender;
  }

  protected ConcurrentMap<RemoteCacheMessage, OperationResult<?>> getActiveOperations() {
    return _activeOperations;
  }

  protected ConcurrentMap<Long, OperationResult<?>> getPendingOperations() {
    return _pendingOperations;
  }

  public long getValueSpecificationId(ValueSpecification valueSpec) {
    ArgumentChecker.notNull(valueSpec, "Value Specification");
    Long result = _specificationIds.get(valueSpec);
    if (result != null) {
      return result;
    }
    final OperationResult<ValueSpecificationLookupResponse> lookupResult = remoteLookupValueSpecificationId(valueSpec);
    if (lookupResult == null) {
      throw new OpenGammaRuntimeException("Couldn't lookup value specification ID for " + valueSpec);
    }
    result = lookupResult.getResult().getSpecificationId();
    Long previousResult = _specificationIds.putIfAbsent(valueSpec, result);
    lookupResult.release();
    assert (previousResult == null) || (previousResult == result) : "Inconsistent results from concurrent spec requests!";
    return result;
  }

  protected Long getNextCorrelationId() {
    return _correlationId.getAndIncrement();
  }

  @SuppressWarnings("unchecked")
  protected <T> OperationResult<T> sendMessageAndWait(final RemoteCacheMessage message, final boolean coalesce) {
    final OperationResult<T> result = new OperationResult<T>(message);
    if (coalesce) {
      final OperationResult<T> pending = (OperationResult<T>) getActiveOperations().putIfAbsent(message, result);
      if (pending != null) {
        s_logger.debug("Waiting for result from other calling thread");
        return pending.await() ? pending : null;
      }
    }
    final RemoteCacheMessage correlatedMessage = coalesce ? message.clone() : message;
    final Long correlationId = getNextCorrelationId();
    correlatedMessage.setCorrelationId(correlationId);
    getPendingOperations().put(correlationId, result);
    s_logger.debug("Calling server with correlation ID {}", correlationId);
    final FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    final MutableFudgeFieldContainer request = ctx.objectToFudgeMsg(correlatedMessage);
    FudgeSerializationContext.addClassHeader(request, message.getClass(), RemoteCacheMessage.class);
    synchronized (getRequestSender()) {
      getRequestSender().sendRequest(request, this);
    }
    final boolean success = result.await();
    s_logger.debug("Removing correlation ID {} from pending operations", correlatedMessage.getCorrelationId());
    getPendingOperations().remove(correlationId);
    if (success) {
      return result;
    }
    s_logger.warn("Remote server timed out on {}", message);
    getActiveOperations().remove(message);
    return null;
  }

  protected OperationResult<ValueSpecificationLookupResponse> remoteLookupValueSpecificationId(ValueSpecification valueSpec) {
    ArgumentChecker.notNull(valueSpec, "Value Specification");
    s_logger.info("Requesting value specification ID for {}", valueSpec);
    final ValueSpecificationLookupRequest request = new ValueSpecificationLookupRequest(valueSpec);
    final OperationResult<ValueSpecificationLookupResponse> response = sendMessageAndWait(request, true);
    if (response == null) {
      throw new OpenGammaRuntimeException("Couldn't lookup value specification ID for " + valueSpec);
    }
    s_logger.debug("Specification ID {}", response.getResult().getSpecificationId());
    return response;
  }

  public OperationResult<ValueLookupResponse> getValue(String viewName, String calcConfigName, long timestamp, ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(viewName, "viewName");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    s_logger.info("Requesting value {}", valueSpecification);
    final long specificationId = getValueSpecificationId(valueSpecification);
    s_logger.debug("Requesting value by ID {}", specificationId);
    final ValueLookupRequest request = new ValueLookupRequest(viewName, calcConfigName, timestamp, specificationId);
    final OperationResult<ValueLookupResponse> response = sendMessageAndWait(request, true);
    if (response == null) {
      throw new OpenGammaRuntimeException("Couldn't get value " + valueSpecification);
    }
    s_logger.debug("Value = {}", response.getResult().getValue());
    return response;
  }

  public void putValue(String viewName, String calcConfigName, long timestamp, ComputedValue value) {
    ArgumentChecker.notNull(viewName, "viewName");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(value, "computedValue");
    s_logger.info("Submitting value {}", value.getSpecification());
    final long specificationId = getValueSpecificationId(value.getSpecification());
    final ValuePutRequest request = new ValuePutRequest(viewName, calcConfigName, timestamp, specificationId, value.getValue());
    final OperationResult<ValuePutResponse> response = sendMessageAndWait(request, false);
    if (response == null) {
      throw new OpenGammaRuntimeException("Couldn't put value " + value.getSpecification());
    }
  }

  public void purgeCache(String viewName, long timestamp) {
    ArgumentChecker.notNull(viewName, "viewName");
    s_logger.info("Submitting Purge {} {}", viewName, timestamp);

    final CachePurgeRequest request = new CachePurgeRequest(viewName, timestamp);
    final OperationResult<CachePurgeResponse> response = sendMessageAndWait(request, true);
    if (response == null) {
      throw new OpenGammaRuntimeException("Couldn't purge cache");
    }
    response.release();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext ctx = new FudgeDeserializationContext(fudgeContext);
    final RemoteCacheMessage message = ctx.fudgeMsgToObject(RemoteCacheMessage.class, msgEnvelope.getMessage());
    final Long correlationId = message.getCorrelationId();
    s_logger.info("Releasing correlation ID {}", correlationId);
    final OperationResult<RemoteCacheMessage> result = (OperationResult<RemoteCacheMessage>) getPendingOperations().get(correlationId);
    if (result != null) {
      result._result = message;
      result._latch.countDown();
    } else {
      s_logger.warn("Correlation ID {} not found in pending operation map", correlationId);
    }
  }
}
