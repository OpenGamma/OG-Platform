/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;

/**
 * Client end to RemoteNodeServer for registering one or more AbstractCalculationNodes with a remote job dispatcher.
 */
public class RemoteNodeClient extends AbstractCalculationNodeInvocationContainer<BlockingQueue<AbstractCalculationNode>> implements FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeClient.class);

  private final FudgeConnection _connection;
  private final ExecutorService _executorService = Executors.newCachedThreadPool();

  public RemoteNodeClient(final FudgeConnection connection) {
    super(new LinkedBlockingQueue<AbstractCalculationNode>());
    _connection = connection;
    connection.setFudgeMessageReceiver(this);
  }

  public RemoteNodeClient(final FudgeConnection connection, final AbstractCalculationNode node) {
    this(connection);
    setNode(node);
  }

  public RemoteNodeClient(final FudgeConnection connection, final Collection<AbstractCalculationNode> nodes) {
    this(connection);
    setNodes(nodes);
  }

  @Override
  public void onNodeChange() {
    sendCapabilities();
  }

  protected FudgeConnection getConnection() {
    return _connection;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  private void sendMessage(final RemoteCalcNodeMessage message) {
    final FudgeMessageSender sender = getConnection().getFudgeMessageSender();
    final FudgeSerializationContext context = new FudgeSerializationContext(sender.getFudgeContext());
    final FudgeFieldContainer msg = FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(message), message.getClass(), RemoteCalcNodeMessage.class);
    s_logger.debug("Sending message {}", msg);
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
    s_logger.debug("Received {}", msg);
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage message = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    if (message instanceof RemoteCalcNodeJobMessage) {
      handleJobMessage((RemoteCalcNodeJobMessage) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
  }

  private void handleJobMessage(final RemoteCalcNodeJobMessage message) {
    getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        try {
          final CalculationJobResult result = getNodes().take().executeJob(message.getJob());
          sendMessage(new RemoteCalcNodeResultMessage(result));
        } catch (InterruptedException e) {
          s_logger.warn("Thread interrupted");
        }
      }
    });
  }

}
