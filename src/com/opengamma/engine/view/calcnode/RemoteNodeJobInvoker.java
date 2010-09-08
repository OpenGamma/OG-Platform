/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeBusyMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeFailureMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeJobMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeReadyMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeResultMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.monitor.OperationTimer;

/**
 * A JobInvoker for invoking a job on a remote node connected by a FudgeConnection.
 */
/* package */class RemoteNodeJobInvoker implements JobInvoker, FudgeMessageReceiver, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeJobInvoker.class);

  private final ConcurrentMap<CalculationJobSpecification, JobInvocationReceiver> _jobCompletionCallbacks = new ConcurrentHashMap<CalculationJobSpecification, JobInvocationReceiver>();
  private final ExecutorService _executorService;
  private final FudgeMessageSender _fudgeMessageSender;
  private final CapabilitySet _capabilitySet = new CapabilitySet();
  private volatile int _capacity;
  private final AtomicInteger _launched = new AtomicInteger();
  private final AtomicReference<JobInvokerRegister> _dispatchCallback = new AtomicReference<JobInvokerRegister>();
  private final IdentifierMap _identifierMap;
  private volatile String _invokerId;

  public RemoteNodeJobInvoker(final ExecutorService executorService, final RemoteCalcNodeReadyMessage initialMessage, final FudgeConnection fudgeConnection, final IdentifierMap identifierMap) {
    _executorService = executorService;
    _fudgeMessageSender = fudgeConnection.getFudgeMessageSender();
    _identifierMap = identifierMap;
    _invokerId = fudgeConnection.toString();
    fudgeConnection.setFudgeMessageReceiver(this);
    fudgeConnection.setConnectionStateListener(this);
    handleReadyMessage(initialMessage);
    s_logger.info("Remote node invoker created with capacity {}", _capacity);
  }

  private CapabilitySet getCapabilitySet() {
    return _capabilitySet;
  }

  protected void addCapabilities(final Collection<Capability> capabilities) {
    getCapabilitySet().addCapabilities(capabilities);
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return getCapabilitySet().getCapabilities();
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
  public boolean invoke(final CalculationJob rootJob, final JobInvocationReceiver receiver) {
    if (_launched.incrementAndGet() > _capacity) {
      _launched.decrementAndGet();
      s_logger.debug("Capacity reached");
      return false;
    }
    s_logger.info("Dispatching job {}", rootJob.getSpecification());
    // Don't block the dispatcher with outgoing serialization and I/O
    getExecutorService().execute(new Runnable() {

      private void sendJob(final CalculationJob job) {
        try {
          getJobCompletionCallbacks().put(job.getSpecification(), receiver);
          final OperationTimer timer = new OperationTimer(s_logger, "Invocation serialisation and send of job {}", job.getSpecification().getJobId());
          job.convertInputs(getIdentifierMap());
          final RemoteCalcNodeJobMessage message = new RemoteCalcNodeJobMessage(job);
          final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeMessageSender().getFudgeContext());
          getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class));
          timer.finished();
        } catch (Exception e) {
          s_logger.warn("Error sending job {}", job.getSpecification().getJobId());
          _launched.decrementAndGet();
          receiver.jobFailed(RemoteNodeJobInvoker.this, "node on " + getInvokerId(), new OpenGammaRuntimeException("Error sending job", e));
        }
      }

      private void sendJobTail(final CalculationJob job) {
        if (job.getTail() != null) {
          for (CalculationJob tail : job.getTail()) {
            sendJob(tail);
          }
          for (CalculationJob tail : job.getTail()) {
            sendJobTail(tail);
          }
        }
      }

      @Override
      public void run() {
        // Breadth first sending of jobs, just in case some can start before we've sent everything
        sendJob(rootJob);
        sendJobTail(rootJob);
      }
    });
    return true;
  }

  @Override
  public boolean notifyWhenAvailable(final JobInvokerRegister callback) {
    _dispatchCallback.set(callback);
    if (_launched.get() < _capacity) {
      if (registerIfRequired(false)) {
        s_logger.debug("Capacity available at notify");
        return true;
      }
    }
    return false;
  }

  private boolean registerIfRequired(final boolean invokeCallback) {
    final JobInvokerRegister callback = _dispatchCallback.getAndSet(null);
    if (callback != null) {
      if (invokeCallback) {
        callback.registerJobInvoker(this);
      }
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
    } else if (message instanceof RemoteCalcNodeBusyMessage) {
      handleBusyMessage((RemoteCalcNodeBusyMessage) message);
    } else if (message instanceof RemoteCalcNodeReadyMessage) {
      handleReadyMessage((RemoteCalcNodeReadyMessage) message);
    } else if (message instanceof RemoteCalcNodeFailureMessage) {
      handleFailureMessage((RemoteCalcNodeFailureMessage) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
  }

  private void handleResultMessage(final RemoteCalcNodeResultMessage message) {
    s_logger.info("Received result for job {}", message.getResult().getSpecification());
    if (message.getReady() != null) {
      handleReadyMessage(message.getReady());
    }
    // We check for below capacity. We can get "equal" here, but that means there is an invoke taking place which will be dealt with
    // by the notifyWhenAvailable that gets called to reschedule the invoker
    if (_launched.decrementAndGet() < _capacity) {
      if (registerIfRequired(true)) {
        s_logger.debug("Notified dispatcher of capacity available");
      }
    }
    // We decrement the count (and re-register) before processing the data as the remote node is already available if it's sent us its data.
    final JobInvocationReceiver receiver = getJobCompletionCallbacks().remove(message.getResult().getSpecification());
    if (receiver != null) {
      final CalculationJobResult result = message.getResult();
      result.resolveInputs(getIdentifierMap());
      receiver.jobCompleted(result);
    } else {
      s_logger.warn("Duplicate or result for cancelled callback {} received", message.getResult().getSpecification());
    }
  }

  private void handleBusyMessage(final RemoteCalcNodeBusyMessage message) {
    s_logger.debug("Remote calc node on {} started a tail job", this);
    _launched.incrementAndGet();
  }

  private void handleReadyMessage(final RemoteCalcNodeReadyMessage message) {
    s_logger.debug("Remote invoker ready message - {}", message);
    getCapabilitySet().setParameterCapability(PlatformCapabilities.NODE_COUNT, message.getCapacity());
    // [ENG-42] this is where we'd detect any other capability changes
    _capacity = message.getCapacity();
    final int launched = _launched.get();
    if (launched < _capacity) {
      if (registerIfRequired(true)) {
        s_logger.info("Remote invoker ready for use by dispatcher, capacity {}", message.getCapacity());
      }
    } else {
      s_logger.info("Remote invoker over capacity {} with {} jobs", message.getCapacity(), launched);
    }
  }

  private void handleFailureMessage(final RemoteCalcNodeFailureMessage message) {
    s_logger.info("Received failure for job {}", message.getJob());
    if (message.getReady() != null) {
      handleReadyMessage(message.getReady());
    }
    // We check for below capacity. We can get "equal" here, but that means there is an invoke taking place which will be dealt with
    // by the notifyWhenAvailable that gets called to reschedule the invoker
    if (_launched.decrementAndGet() < _capacity) {
      if (registerIfRequired(true)) {
        s_logger.debug("Notified dispatcher of capacity available");
      }
    }
    // We decrement the count (and re-register) before processing the data as the remote node is already available if it's sent us its data.
    final JobInvocationReceiver receiver = getJobCompletionCallbacks().remove(message.getJob());
    if (receiver != null) {
      s_logger.debug("Failed job on {} with message {}", message.getComputeNodeId(), message.getErrorMessage());
      receiver.jobFailed(this, message.getComputeNodeId(), new OpenGammaRuntimeException(message.getErrorMessage()));
    } else {
      s_logger.warn("Duplicate or failure for cancelled callback {} received", message.getJob());
    }
  }

  @Override
  public void connectionFailed(final FudgeConnection connection, final Exception cause) {
    s_logger.warn("Client connection {} dropped", connection, cause);
    _launched.addAndGet(_capacity);
    _invokerId = null;
    for (CalculationJobSpecification jobSpec : getJobCompletionCallbacks().keySet()) {
      final JobInvocationReceiver callback = getJobCompletionCallbacks().remove(jobSpec);
      // There could still be late messages arriving from a buffer even though the connection has now failed
      if (callback != null) {
        s_logger.debug("Cancelling pending operation {}", jobSpec);
        callback.jobFailed(this, "node on " + getInvokerId(), cause);
      }
    }
  }

  @Override
  public void connectionReset(final FudgeConnection connection) {
    s_logger.info("Connection reset by client");
    // We're the server end of a connection, so this isn't going to happen with the socket implementation
  }

  @Override
  public String toString() {
    return _fudgeMessageSender.toString();
  }

  @Override
  public String getInvokerId() {
    return _invokerId;
  }

}
