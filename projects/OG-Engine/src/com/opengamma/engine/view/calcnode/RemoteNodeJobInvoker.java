/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.calcnode.msg.Busy;
import com.opengamma.engine.view.calcnode.msg.Cancel;
import com.opengamma.engine.view.calcnode.msg.Execute;
import com.opengamma.engine.view.calcnode.msg.Failure;
import com.opengamma.engine.view.calcnode.msg.Invocations;
import com.opengamma.engine.view.calcnode.msg.IsAlive;
import com.opengamma.engine.view.calcnode.msg.Ready;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessageVisitor;
import com.opengamma.engine.view.calcnode.msg.Result;
import com.opengamma.engine.view.calcnode.msg.Scaling;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsReceiver;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

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
  private final FunctionCosts _functionCosts;
  private volatile String _invokerId;
  private final RemoteCalcNodeMessageVisitor _messageVisitor = new RemoteCalcNodeMessageVisitor() {

    @Override
    protected void visitUnexpectedMessage(final RemoteCalcNodeMessage message) {
      s_logger.warn("Unexpected message - {}", message);
    }

    @Override
    protected void visitBusyMessage(final Busy message) {
      s_logger.debug("Remote calc node on {} started a tail job", this);
      _launched.incrementAndGet();
    }

    @Override
    protected void visitFailureMessage(final Failure message) {
      s_logger.info("Received failure for job {}", message.getJob());
      if (message.getReady() != null) {
        message.getReady().accept(this);
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
        receiver.jobFailed(RemoteNodeJobInvoker.this, message.getComputeNodeId(), new OpenGammaRuntimeException(message.getErrorMessage()));
      } else {
        s_logger.warn("Duplicate or failure for cancelled callback {} received", message.getJob());
      }
    }

    @Override
    protected void visitInvocationsMessage(final Invocations message) {
      s_logger.info("Received invocation statistics");
      final Scaling scaling = FunctionInvocationStatisticsReceiver.messageReceived(getFunctionCosts(), message);
      if (scaling != null) {
        s_logger.debug("Sending scaling message ", scaling);
        final MutableFudgeFieldContainer scalingMessage = getFudgeMessageSender().getFudgeContext().newMessage();
        FudgeSerializationContext.addClassHeader(scalingMessage, scaling.getClass(), RemoteCalcNodeMessage.class);
        scaling.toFudgeMsg(getFudgeMessageSender().getFudgeContext(), scalingMessage);
        getFudgeMessageSender().send(scalingMessage);
      }
    }

    @Override
    protected void visitReadyMessage(final Ready message) {
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

    @Override
    protected void visitResultMessage(final Result message) {
      s_logger.info("Received result for job {}", message.getResult().getSpecification());
      if (message.getReady() != null) {
        message.getReady().accept(this);
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

  };

  public RemoteNodeJobInvoker(
      final ExecutorService executorService, final Ready initialMessage, final FudgeConnection fudgeConnection,
      final IdentifierMap identifierMap, final FunctionCosts functionCosts) {
    _executorService = executorService;
    _fudgeMessageSender = fudgeConnection.getFudgeMessageSender();
    _identifierMap = identifierMap;
    _invokerId = fudgeConnection.toString();
    _functionCosts = functionCosts;
    fudgeConnection.setFudgeMessageReceiver(this);
    fudgeConnection.setConnectionStateListener(this);
    initialMessage.accept(_messageVisitor);
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

  private FunctionCosts getFunctionCosts() {
    return _functionCosts;
  }

  protected void sendMessage(final RemoteCalcNodeMessage message) {
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeMessageSender().getFudgeContext());
    getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class));
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
          job.convertInputs(getIdentifierMap());
          sendMessage(new Execute(job));
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
  public void cancel(final Collection<CalculationJobSpecification> jobs) {
    s_logger.info("Cancelling {} jobs at {}", jobs.size(), getInvokerId());
    sendMessage(new Cancel(jobs));
  }

  /**
   * Returns {@code true} with the remote client generating failure messages if anything is
   * not alive. 
   */
  @Override
  public boolean isAlive(final Collection<CalculationJobSpecification> jobs) {
    s_logger.info("Querying {} jobs at {}", jobs.size(), getInvokerId());
    sendMessage(new IsAlive(jobs));
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
    message.accept(_messageVisitor);
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
