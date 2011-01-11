/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.calcnode.msg.Busy;
import com.opengamma.engine.view.calcnode.msg.Cancel;
import com.opengamma.engine.view.calcnode.msg.Execute;
import com.opengamma.engine.view.calcnode.msg.Failure;
import com.opengamma.engine.view.calcnode.msg.Init;
import com.opengamma.engine.view.calcnode.msg.IsAlive;
import com.opengamma.engine.view.calcnode.msg.Ready;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessageVisitor;
import com.opengamma.engine.view.calcnode.msg.Result;
import com.opengamma.engine.view.calcnode.msg.Scaling;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsSender;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * Client end to RemoteNodeServer for registering one or more AbstractCalculationNodes with a remote job dispatcher. The connection must
 * deliver messages in network order (i.e. not use an executor service).
 */
public class RemoteNodeClient extends AbstractCalculationNodeInvocationContainer implements FudgeMessageReceiver, Lifecycle, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeClient.class);

  private final FudgeConnection _connection;
  private final CompiledFunctionService _functionCompilationService;
  private final IdentifierMap _identifierMap;
  private final FunctionInvocationStatisticsSender _statistics;
  private boolean _started;
  private final RemoteCalcNodeMessageVisitor _messageVisitor = new RemoteCalcNodeMessageVisitor() {

    @Override
    protected void visitUnexpectedMessage(final RemoteCalcNodeMessage message) {
      s_logger.warn("Unexpected message - {}", message);
    }

    @Override
    protected void visitCancelMessage(final Cancel message) {
      for (CalculationJobSpecification job : message.getJob()) {
        cancelJob(job);
      }
    }

    @Override
    protected void visitExecuteMessage(final Execute message) {
      final CalculationJob job = message.getJob();
      getFunctionCompilationService().reinitializeIfNeeded(job.getFunctionInitializationIdentifier());
      job.resolveInputs(getIdentifierMap());
      addJob(job, new ExecutionReceiver() {

        @Override
        public void executionComplete(final CalculationJobResult result) {
          result.convertInputs(getIdentifierMap());
          sendMessage(new Result(result));
        }

        @Override
        public void executionFailed(final AbstractCalculationNode node, final Exception exception) {
          s_logger.warn("Exception thrown by job execution", exception);
          sendMessage(new Failure(job.getSpecification(), exception.getMessage(), node.getNodeId()));
        }

      }, null);
    }

    @Override
    protected void visitInitMessage(final Init message) {
      // Note that this may be called multiple times, e.g. after a reconnect.
      getFunctionCompilationService().initialize(message.getFunctionInitId());
    }

    @Override
    protected void visitIsAliveMessage(final IsAlive message) {
      for (CalculationJobSpecification job : message.getJob()) {
        if (!isJobAlive(job)) {
          sendMessage(new Failure(job, "isAlive returned false", ""));
        }
      }
    }

    @Override
    protected void visitScalingMessage(final Scaling message) {
      s_logger.info("Scaling data received {}", message);
      getStatistics().setScaling(message.getInvocation());
    }

  };

  public RemoteNodeClient(final FudgeConnection connection, final CompiledFunctionService functionCompilationService, final IdentifierMap identifierMap,
      final FunctionInvocationStatisticsSender statistics) {
    _connection = connection;
    _functionCompilationService = functionCompilationService;
    _identifierMap = identifierMap;
    connection.setFudgeMessageReceiver(this);
    _statistics = statistics;
    statistics.setExecutorService(getExecutorService());
    statistics.setFudgeMessageSender(connection.getFudgeMessageSender());
  }

  public RemoteNodeClient(final FudgeConnection connection, final CompiledFunctionService functionCompilationService, final IdentifierMap identifierMap,
      final FunctionInvocationStatisticsSender statistics, final AbstractCalculationNode node) {
    this(connection, functionCompilationService, identifierMap, statistics);
    setNode(node);
  }

  public RemoteNodeClient(final FudgeConnection connection, final CompiledFunctionService functionCompilationService, final IdentifierMap identifierMap,
      final FunctionInvocationStatisticsSender statistics, final Collection<AbstractCalculationNode> nodes) {
    this(connection, functionCompilationService, identifierMap, statistics);
    setNodes(nodes);
  }

  @Override
  public void onNodeChange() {
    if (isRunning()) {
      sendCapabilities();
    }
  }

  protected FudgeConnection getConnection() {
    return _connection;
  }

  protected CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  protected IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  protected FunctionInvocationStatisticsSender getStatistics() {
    return _statistics;
  }

  private void sendMessage(final RemoteCalcNodeMessage message) {
    final FudgeMessageSender sender = getConnection().getFudgeMessageSender();
    final FudgeSerializationContext context = new FudgeSerializationContext(sender.getFudgeContext());
    final FudgeFieldContainer msg = FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class);
    s_logger.debug("Sending message ({} fields) to {}", msg.getNumFields(), _connection);
    sender.send(msg);
  }

  protected void sendCapabilities() {
    final Ready ready = new Ready(getNodes().size());
    // TODO any other capabilities to add
    sendMessage(ready);
  }

  /**
   * Needs to run sequentially, preserving network message order.
   * 
   * @param fudgeContext the Fudge context for processing the message
   * @param msgEnvelope the received message
   */
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final FudgeFieldContainer msg = msgEnvelope.getMessage();
    s_logger.debug("Received ({} fields) from {}", msg.getNumFields(), _connection);
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage message = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    message.accept(_messageVisitor);
  }

  @Override
  protected void onJobStart(final CalculationJob job) {
    if (job.getRequiredJobIds() != null) {
      sendMessage(new Busy());
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    if (!_started) {
      s_logger.info("Client starting");
      sendCapabilities();
      _started = true;
      s_logger.info("Client started for {}", _connection);
      _connection.setConnectionStateListener(this);
    } else {
      s_logger.warn("Client already started");
    }
  }

  @Override
  public synchronized void stop() {
    if (_started) {
      s_logger.info("Client stopped");
      _connection.setConnectionStateListener(null);
      _started = false;
    } else {
      s_logger.warn("Client already stopped");
    }
  }

  @Override
  public void connectionFailed(final FudgeConnection connection, final Exception cause) {
    s_logger.warn("Underlying connection failed - client cannot run", cause);
    if (_started) {
      stop();
    }
  }

  @Override
  public void connectionReset(final FudgeConnection connection) {
    s_logger.info("Underlying connection reset - resending capabilities");
    sendCapabilities();
    s_logger.debug("Capabilities sent");
  }

}
