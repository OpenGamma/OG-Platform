/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * A JobInvoker for invoking a job on a remote node connected by a FudgeConnection.
 */
/* package */class RemoteNodeJobInvoker implements JobInvoker, FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeJobInvoker.class);

  private static final int DEFAULT_PRIORITY = 10;

  private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _jobCompletionCallbacks = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
  private final ExecutorService _executorService;
  private final FudgeMessageSender _fudgeMessageSender;
  private int _nodePriority = DEFAULT_PRIORITY;
  private volatile int _capacity;
  private final AtomicInteger _launched = new AtomicInteger();
  private final AtomicReference<JobInvokerRegister> _dispatchCallback = new AtomicReference<JobInvokerRegister>();

  public RemoteNodeJobInvoker(final ExecutorService executorService, final RemoteCalcNodeReadyMessage initialMessage, final FudgeConnection fudgeConnection) {
    _executorService = executorService;
    _fudgeMessageSender = fudgeConnection.getFudgeMessageSender();
    fudgeConnection.setFudgeMessageReceiver(this);
    handleReadyMessage(initialMessage);
    s_logger.info("Remote node invoker created with capacity {}", _capacity);
  }

  public void setNodePriority(final int nodePriority) {
    _nodePriority = nodePriority;
  }

  public int getNodePriority() {
    return _nodePriority;
  }

  private ConcurrentMap<CalculationJobSpecification, JobResultReceiver> getJobCompletionCallbacks() {
    return _jobCompletionCallbacks;
  }

  private FudgeMessageSender getFudgeMessageSender() {
    return _fudgeMessageSender;
  }

  private ExecutorService getExecutorService() {
    return _executorService;
  }

  @Override
  public int canInvoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items) {
    // TODO this is where we'd consider capabilities
    return getNodePriority();
  }

  @Override
  public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver receiver) {
    if (_launched.incrementAndGet() > _capacity) {
      _launched.decrementAndGet();
      s_logger.debug("Capacity reached");
      return false;
    }
    s_logger.info("Dispatching job {}", jobSpec);
    // Don't block the dispatcher with outgoing serialisation and I/O
    getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        getJobCompletionCallbacks().put(jobSpec, receiver);
        final RemoteCalcNodeJobMessage message = new RemoteCalcNodeJobMessage(new CalculationJob(jobSpec, items));
        final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeMessageSender().getFudgeContext());
        getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class));
      }
    });
    return true;
  }
  
  // TODO detect the loss of a FudgeConnection and any "pending" jobs should be passed back to their result receiver as failed so that they can be retried

  @Override
  public void notifyWhenAvailable(final JobInvokerRegister callback) {
    _dispatchCallback.set(callback);
    if (_launched.get() < _capacity) {
      if (registerIfRequired()) {
        s_logger.debug("Capacity available at notify");
      }
    }
  }

  private boolean registerIfRequired() {
    final JobInvokerRegister callback = _dispatchCallback.getAndSet(null);
    if (callback != null) {
      callback.registerJobInvoker(this);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage message = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    if (message instanceof RemoteCalcNodeResultMessage) {
      handleResultMessage((RemoteCalcNodeResultMessage) message);
    } else if (message instanceof RemoteCalcNodeReadyMessage) {
      handleReadyMessage((RemoteCalcNodeReadyMessage) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
  }

  private void handleResultMessage(final RemoteCalcNodeResultMessage message) {
    s_logger.info("Received result for job {}", message.getResult().getSpecification());
    // We check for below capacity. We can get "equal" here, but that means there is an invoke taking place which will be dealt with
    // by the notifyWhenAvailable that gets called to reschedule the invoker
    if (message.getReady() != null) {
      handleReadyMessage(message.getReady());
    }
    if (_launched.decrementAndGet() < _capacity) {
      if (registerIfRequired()) {
        s_logger.debug("Notified dispatcher of capacity available");
      }
    }
    // We decrement the count (and re-register) first as the remote node is already available if it's sent us its data. Note that
    // we could split the messages and re-register before the node starts sending data so it's next job can be overlaid (assumes
    // duplex network).

    final JobResultReceiver receiver = getJobCompletionCallbacks().remove(message.getResult().getSpecification());
    s_logger.debug("Passing result back to {}", receiver);
    receiver.resultReceived(message.getResult());
  }

  private void handleReadyMessage(final RemoteCalcNodeReadyMessage message) {
    s_logger.debug("Remote invoker ready message - {}", message);
    // TODO this is where we'd detect capability changes
    _capacity = message.getCapacity();
    final int launched = _launched.get();
    if (launched < _capacity) {
      if (registerIfRequired()) {
        s_logger.info("Remote invoker ready for use by dispatcher, capacity {}", message.getCapacity());
      }
    } else {
      s_logger.info("Remote invoker over capacity {} with {} jobs", message.getCapacity(), launched);
    }
  }

}
