/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeInitMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeJobMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeReadyMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeResultMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * Client end to RemoteNodeServer for registering one or more AbstractCalculationNodes with a remote job dispatcher.
 */
public class RemoteNodeClient extends AbstractCalculationNodeInvocationContainer<BlockingQueue<AbstractCalculationNode>> implements FudgeMessageReceiver, Lifecycle, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeClient.class);

  private final FudgeConnection _connection;
  private final FunctionCompilationService _functionCompilationService;
  private final IdentifierMap _identifierMap;
  private boolean _started;

  public RemoteNodeClient(final FudgeConnection connection, final FunctionCompilationService functionCompilationService, final IdentifierMap identifierMap) {
    super(new LinkedBlockingQueue<AbstractCalculationNode>());
    _connection = connection;
    _functionCompilationService = functionCompilationService;
    _identifierMap = identifierMap;
    connection.setFudgeMessageReceiver(this);
  }

  public RemoteNodeClient(final FudgeConnection connection, final FunctionCompilationService functionCompilationService, final IdentifierMap identifierMap, final AbstractCalculationNode node) {
    this(connection, functionCompilationService, identifierMap);
    setNode(node);
  }

  public RemoteNodeClient(final FudgeConnection connection, final FunctionCompilationService functionCompilationService, final IdentifierMap identifierMap,
      final Collection<AbstractCalculationNode> nodes) {
    this(connection, functionCompilationService, identifierMap);
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

  protected FunctionCompilationService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  protected IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  private void sendMessage(final RemoteCalcNodeMessage message) {
    final FudgeMessageSender sender = getConnection().getFudgeMessageSender();
    final FudgeSerializationContext context = new FudgeSerializationContext(sender.getFudgeContext());
    final FudgeFieldContainer msg = FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class);
    s_logger.debug("Sending message ({} fields) to {}", msg.getNumFields(), _connection);
    sender.send(msg);
  }

  protected void sendCapabilities() {
    final RemoteCalcNodeReadyMessage ready = new RemoteCalcNodeReadyMessage(getNodes().size());
    // TODO any other capabilities to add
    sendMessage(ready);
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final FudgeFieldContainer msg = msgEnvelope.getMessage();
    s_logger.debug("Received ({} fields) from {}", msg.getNumFields(), _connection);
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage message = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    if (message instanceof RemoteCalcNodeJobMessage) {
      handleJobMessage((RemoteCalcNodeJobMessage) message);
    } else if (message instanceof RemoteCalcNodeInitMessage) {
      handleInitMessage((RemoteCalcNodeInitMessage) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
  }

  private void handleJobMessage(final RemoteCalcNodeJobMessage message) {
    try {
      final CalculationJob job = message.getJob();
      job.resolveInputs(getIdentifierMap());
      final AbstractCalculationNode node = getNodes().take();
      CalculationJobResult result = null;
      try {
        result = node.executeJob(job);
      } catch (Exception e) {
        s_logger.warn("Exception thrown by job execution", e);
        // TODO [ENG-204] propogate this error back to the server
      }
      getNodes().add(node);
      if (result != null) {
        result.convertInputs(getIdentifierMap());
        sendMessage(new RemoteCalcNodeResultMessage(result));
      }
    } catch (InterruptedException e) {
      s_logger.warn("Thread interrupted");
    }
  }

  private void handleInitMessage(final RemoteCalcNodeInitMessage message) {
    // TODO
  }

  @Override
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    if (!_started) {
      getFunctionCompilationService().initialize(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
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
