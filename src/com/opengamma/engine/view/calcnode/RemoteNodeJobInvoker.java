/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * A JobInvoker for invoking a job on a remote node connected by a FudgeConnection.
 */
/* package */class RemoteNodeJobInvoker implements JobInvoker, FudgeMessageReceiver, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeJobInvoker.class);

  private final ConcurrentMap<CalculationJobSpecification, JobInvocationReceiver> _jobCompletionCallbacks = new ConcurrentHashMap<CalculationJobSpecification, JobInvocationReceiver>();
  private final ExecutorService _executorService;
  private final FudgeMessageSender _fudgeMessageSender;
  private final Set<Capability> _capabilities = new HashSet<Capability>();
  private volatile int _capacity;
  private final AtomicInteger _launched = new AtomicInteger();
  private final AtomicReference<JobInvokerRegister> _dispatchCallback = new AtomicReference<JobInvokerRegister>();
  private final IdentifierMap _identifierMap;

  public RemoteNodeJobInvoker(final ExecutorService executorService, final RemoteCalcNodeReadyMessage initialMessage, final FudgeConnection fudgeConnection, final IdentifierMap identifierMap) {
    _executorService = executorService;
    _fudgeMessageSender = fudgeConnection.getFudgeMessageSender();
    _identifierMap = identifierMap;
    fudgeConnection.setFudgeMessageReceiver(this);
    fudgeConnection.setConnectionStateListener(this);
    handleReadyMessage(initialMessage);
    s_logger.info("Remote node invoker created with capacity {}", _capacity);
  }

  public void addCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    getCapabilities().add(capability);
  }

  public void addCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    getCapabilities().addAll(capabilities);
  }

  public void removeCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    getCapabilities().removeAll(capabilities);
  }

  public void setCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    getCapabilities().clear();
    getCapabilities().addAll(capabilities);
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return _capabilities;
  }

  private ConcurrentMap<CalculationJobSpecification, JobInvocationReceiver> getJobCompletionCallbacks() {
    return _jobCompletionCallbacks;
  }

  private FudgeMessageSender getFudgeMessageSender() {
    return _fudgeMessageSender;
  }

  private ExecutorService getExecutorService() {
    return _executorService;
  }

  private IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  @Override
  public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobInvocationReceiver receiver) {
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
        final OperationTimer timer = new OperationTimer(s_logger, "Invocation serialisation and send of job {}", jobSpec.getJobId());
        final CalculationJob job = new CalculationJob(jobSpec, items);
        job.convertInputs(getIdentifierMap());
        final RemoteCalcNodeJobMessage message = new RemoteCalcNodeJobMessage(job);
        final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeMessageSender().getFudgeContext());
        getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class));
        timer.finished();
      }
    });
    return true;
  }

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

    final JobInvocationReceiver receiver = getJobCompletionCallbacks().remove(message.getResult().getSpecification());
    if (receiver != null) {
      final CalculationJobResult result = message.getResult();
      result.resolveInputs(getIdentifierMap());
      receiver.jobCompleted(result);
    } else {
      s_logger.warn("Duplicate or result for cancelled callback {} received", message.getResult().getSpecification());
    }
  }

  private void handleReadyMessage(final RemoteCalcNodeReadyMessage message) {
    s_logger.debug("Remote invoker ready message - {}", message);
    // [ENG-42] this is where we'd detect capability changes
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

  @Override
  public void connectionFailed(final FudgeConnection connection, final Exception cause) {
    s_logger.warn("Client connection {} dropped", connection, cause);
    for (CalculationJobSpecification jobSpec : getJobCompletionCallbacks().keySet()) {
      final JobInvocationReceiver callback = getJobCompletionCallbacks().remove(jobSpec);
      // There could still be late messages arriving from a buffer even though the connection has now failed
      if (callback != null) {
        s_logger.debug("Cancelling pending operation {}", jobSpec);
        callback.jobFailed(this, cause);
      }
    }
  }

  @Override
  public void connectionReset(final FudgeConnection connection) {
    s_logger.info("Connection reset by client");
    // We're the server end of a connection, so this isn't going to happen with the socket implementation
  }

}
