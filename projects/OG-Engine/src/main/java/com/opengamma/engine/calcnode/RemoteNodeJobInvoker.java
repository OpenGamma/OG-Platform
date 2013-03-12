/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.cache.AbstractIdentifierMap;
import com.opengamma.engine.cache.IdentifierMap;
import com.opengamma.engine.calcnode.msg.Cancel;
import com.opengamma.engine.calcnode.msg.Execute;
import com.opengamma.engine.calcnode.msg.Failure;
import com.opengamma.engine.calcnode.msg.Invocations;
import com.opengamma.engine.calcnode.msg.IsAlive;
import com.opengamma.engine.calcnode.msg.Ready;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessageVisitor;
import com.opengamma.engine.calcnode.msg.Result;
import com.opengamma.engine.calcnode.msg.Scaling;
import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsReceiver;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * A JobInvoker for invoking a job on a remote node connected by a FudgeConnection.
 */
/* package */class RemoteNodeJobInvoker implements JobInvoker, FudgeMessageReceiver, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeJobInvoker.class);

  private static final class JobInfo {

    /**
     * The callback to receive notification of the job completion.
     */
    private final JobInvocationReceiver _receiver;

    /**
     * The calculation job.
     */
    private final CalculationJob _job;

    public JobInfo(final JobInvocationReceiver receiver, final CalculationJob job) {
      _receiver = receiver;
      _job = job;
    }

    public JobInvocationReceiver getReceiver() {
      return _receiver;
    }

    public int getLaunchDelta() {
      return (_job.getTail() != null) ? _job.getTail().size() - 1 : -1;
    }

    public CalculationJob getJob() {
      return _job;
    }

  }

  private final ConcurrentMap<CalculationJobSpecification, JobInfo> _pendingJobs = new ConcurrentHashMap<CalculationJobSpecification, JobInfo>();
  private final ExecutorService _executorService;
  private final FudgeMessageSender _fudgeMessageSender;
  private final CapabilitySet _capabilitySet = new CapabilitySet();
  private volatile int _capacity;
  private final AtomicInteger _launched = new AtomicInteger();
  private final AtomicReference<JobInvokerRegister> _dispatchCallback = new AtomicReference<JobInvokerRegister>();
  private final IdentifierMap _identifierMap;
  private final FunctionCosts _functionCosts;
  private final FunctionBlacklistQuery _blacklistQuery;
  private final FunctionBlacklistMaintainer _blacklistUpdate;
  private volatile String _invokerId;
  private final RemoteCalcNodeMessageVisitor _messageVisitor = new RemoteCalcNodeMessageVisitor() {

    @Override
    protected void visitUnexpectedMessage(final RemoteCalcNodeMessage message) {
      s_logger.warn("Unexpected message - {}", message);
    }

    @Override
    protected void visitFailureMessage(final Failure message) {
      s_logger.info("Received failure for job {}", message.getJob());
      if (message.getReady() != null) {
        message.getReady().accept(this);
      }
      // We decrement the count (and re-register) before processing the data as the remote node is already available if it's sent us its data.
      final JobInfo job = getPendingJobs().remove(message.getJob());
      if (job == null) {
        s_logger.warn("Duplicate or failure for cancelled callback {} received", message.getJob());
        return;
      }
      if (_launched.addAndGet(job.getLaunchDelta()) < _capacity) {
        // We check for below capacity. We can get "equal" here, but that means there is an invoke taking place which will be dealt with
        // by the notifyWhenAvailable that gets called to reschedule the invoker
        if (registerIfRequired(true)) {
          s_logger.debug("Notified dispatcher of capacity available");
        }
      }
      s_logger.debug("Failed job on {} with message {}", message.getComputeNodeId(), message.getErrorMessage());
      jobFailed(job, message.getComputeNodeId(), new OpenGammaRuntimeException(message.getErrorMessage()));
    }

    @Override
    protected void visitInvocationsMessage(final Invocations message) {
      s_logger.info("Received invocation statistics");
      final Scaling scaling = FunctionInvocationStatisticsReceiver.messageReceived(getFunctionCosts(), message);
      if (scaling != null) {
        s_logger.debug("Sending scaling message ", scaling);
        final MutableFudgeMsg scalingMessage = getFudgeMessageSender().getFudgeContext().newMessage();
        FudgeSerializer.addClassHeader(scalingMessage, scaling.getClass(), RemoteCalcNodeMessage.class);
        scaling.toFudgeMsg(new FudgeSerializer(getFudgeMessageSender().getFudgeContext()), scalingMessage);
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
      if (launched < 0) {
        // An additional decrement can happen if there is an error in the original job dispatch
        _launched.incrementAndGet();
      } else if (launched < _capacity) {
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
      // We decrement the count (and re-register) before processing the data as the remote node is already available if it's sent us its data.
      final JobInfo job = getPendingJobs().remove(message.getResult().getSpecification());
      if (job == null) {
        s_logger.warn("Duplicate or result for cancelled callback {} received", message.getResult().getSpecification());
        return;
      }
      if (_launched.addAndGet(job.getLaunchDelta()) < _capacity) {
        // We check for below capacity. We can get "equal" here, but that means there is an invoke taking place which will be dealt with
        // by the notifyWhenAvailable that gets called to reschedule the invoker
        if (registerIfRequired(true)) {
          s_logger.debug("Notified dispatcher of capacity available");
        }
      }
      final CalculationJobResult result = message.getResult();
      AbstractIdentifierMap.resolveIdentifiers(getIdentifierMap(), result);
      job.getReceiver().jobCompleted(result);
    }

  };

  public RemoteNodeJobInvoker(
      final ExecutorService executorService, final Ready initialMessage, final FudgeConnection fudgeConnection,
      final IdentifierMap identifierMap, final FunctionCosts functionCosts, final FunctionBlacklistQuery blacklistQuery,
      final FunctionBlacklistMaintainer blacklistUpdate) {
    _executorService = executorService;
    _fudgeMessageSender = fudgeConnection.getFudgeMessageSender();
    _identifierMap = identifierMap;
    _invokerId = initialMessage.getHostId();
    _functionCosts = functionCosts;
    _blacklistQuery = blacklistQuery;
    _blacklistUpdate = blacklistUpdate;
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

  private ConcurrentMap<CalculationJobSpecification, JobInfo> getPendingJobs() {
    return _pendingJobs;
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

  private FunctionBlacklistQuery getBlacklistQuery() {
    return _blacklistQuery;
  }

  private FunctionBlacklistMaintainer getBlacklistUpdate() {
    return _blacklistUpdate;
  }

  protected void sendMessage(final RemoteCalcNodeMessage message) {
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeMessageSender().getFudgeContext());
    getFudgeMessageSender().send(FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class));
  }

  private void jobFailed(final JobInvocationReceiver receiver, final CalculationJob job, final String nodeId, final Exception e) {
    receiver.jobFailed(this, nodeId, e);
    if (job.getTail() == null) {
      if (job.getRequiredJobIds() == null) {
        final Collection<CalculationJobItem> items = job.getJobItems();
        if (items.size() <= 1) {
          getBlacklistUpdate().failedJobItems(items);
        }
      }
    }
  }

  private void jobFailed(final JobInfo job, final String nodeId, final Exception e) {
    jobFailed(job.getReceiver(), job.getJob(), nodeId, e);
  }

  /**
   * Replaces any blacklisted job items with no-op functions. This keeps the shape of the job the same and may allow continuation of dependent jobs that can operate on missing inputs.
   */
  /* package */static CalculationJob blacklist(final FunctionBlacklistQuery query, final CalculationJob job) {
    if (query.isEmpty()) {
      return job;
    }
    final List<CalculationJobItem> originalItems = job.getJobItems();
    final int size = originalItems.size();
    for (int i = 0; i < size; i++) {
      CalculationJobItem item = originalItems.get(i);
      if (query.isBlacklisted(item)) {
        final List<CalculationJobItem> newItems = new ArrayList<CalculationJobItem>(size);
        for (int j = 0; j < i; j++) {
          newItems.add(originalItems.get(j));
        }
        newItems.add(new CalculationJobItem(
            NoOpFunction.UNIQUE_ID, item.getFunctionParameters(), item.getComputationTargetSpecification(),
            item.getInputIdentifiers(), item.getOutputIdentifiers(), ExecutionLogMode.INDICATORS));
        for (int j = i + 1; j < size; j++) {
          item = originalItems.get(i);
          if (query.isBlacklisted(item)) {
            newItems.add(new CalculationJobItem(
                NoOpFunction.UNIQUE_ID, item.getFunctionParameters(), item.getComputationTargetSpecification(),
                item.getInputIdentifiers(), item.getOutputIdentifiers(), ExecutionLogMode.INDICATORS));
          } else {
            newItems.add(item);
          }
        }
        return new CalculationJob(job.getSpecification(), job.getFunctionInitializationIdentifier(), job.getResolverVersionCorrection(), job.getRequiredJobIds(), newItems, job.getCacheSelectHint());
      }
    }
    return job;
  }

  @Override
  public boolean invoke(final CalculationJob rootJob, final JobInvocationReceiver receiver) {
    while (_launched.incrementAndGet() > _capacity) {
      if (_launched.decrementAndGet() >= _capacity) {
        s_logger.debug("Capacity reached");
        return false;
      }
    }
    s_logger.info("Dispatching job {}", rootJob.getSpecification());
    // Don't block the dispatcher with outgoing serialization and I/O
    getExecutorService().execute(new Runnable() {

      private void sendJob(final CalculationJob job) throws Exception {
        getPendingJobs().put(job.getSpecification(), new JobInfo(receiver, job));
        AbstractIdentifierMap.convertIdentifiers(getIdentifierMap(), job);
        sendMessage(new Execute(blacklist(getBlacklistQuery(), job)));
      }

      @Override
      public void run() {
        // Breadth first sending of jobs, just in case some can start before we've sent everything
        try {
          sendJob(rootJob);
          if (rootJob.getTail() != null) {
            final Queue<CalculationJob> jobs = new LinkedList<CalculationJob>(rootJob.getTail());
            CalculationJob job = jobs.poll();
            while (job != null) {
              sendJob(job);
              if (job.getTail() != null) {
                jobs.addAll(job.getTail());
              }
              job = jobs.poll();
            }
          }
        } catch (Exception e) {
          s_logger.warn("Error sending job {}", rootJob.getSpecification().getJobId());
          jobFailed(receiver, rootJob, "node on " + getInvokerId(), e);
          // Not knowing where the failure occurred, we may get an additional decrement if any of the jobs started completing. This may have
          // broken the whole connection which will not be a problem. Otherwise We'll check, and adjust, for this when "Ready" messages
          // arrive.
          if (_launched.decrementAndGet() < _capacity) {
            if (registerIfRequired(true)) {
              s_logger.debug("Notified dispatcher of capacity available");
            }
          }
        }
      }
    });
    return true;
  }

  @Override
  public void cancel(final Collection<CalculationJobSpecification> jobs) {
    s_logger.info("Cancelling {} jobs at {}", jobs.size(), getInvokerId());
    sendMessage(new Cancel(jobs));
  }

  @Override
  public void cancel(final CalculationJobSpecification job) {
    s_logger.info("Cancelling {} at {}", job, getInvokerId());
    sendMessage(new Cancel(Collections.singleton(job)));
  }

  /**
   * Returns true with the remote client generating failure messages if anything is not alive.
   */
  @Override
  public boolean isAlive(final Collection<CalculationJobSpecification> jobs) {
    s_logger.info("Querying {} jobs at {}", jobs.size(), getInvokerId());
    sendMessage(new IsAlive(jobs));
    return true;
  }

  @Override
  public boolean isAlive(final CalculationJobSpecification job) {
    s_logger.info("Querying {} at {}", job.getJobId(), getInvokerId());
    sendMessage(new IsAlive(Collections.singleton(job)));
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
    final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    final RemoteCalcNodeMessage message = deserializer.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    message.accept(_messageVisitor);
  }

  @Override
  public void connectionFailed(final FudgeConnection connection, final Exception cause) {
    s_logger.warn("Client connection {} dropped", connection, cause);
    _launched.addAndGet(_capacity); // Force over capacity to prevent any new submissions
    final String invokerId = _invokerId;
    _invokerId = null;
    for (CalculationJobSpecification jobSpec : getPendingJobs().keySet()) {
      final JobInfo job = getPendingJobs().remove(jobSpec);
      // There could still be late messages arriving from a buffer even though the connection has now failed
      if (job != null) {
        s_logger.debug("Cancelling pending operation {}", jobSpec);
        jobFailed(job, "node on " + invokerId, cause);
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
