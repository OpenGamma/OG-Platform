/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;

/**
 * Server end to RemoteNodeClient to receive requests from remote calculation nodes and marshal
 * them into RemoteNodeJobInvokers that a JobDispatcher can then use.
 */
public class RemoteNodeServer implements FudgeConnectionReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeServer.class);

  private final JobInvokerRegister _jobInvokerRegister;
  private final ExecutorService _executorService = Executors.newCachedThreadPool();
  private FunctionRepository _functionRepository;

  private Integer _nodePriority;

  public RemoteNodeServer(final JobInvokerRegister jobInvokerRegister, final FunctionRepository functionRepository) {
    _jobInvokerRegister = jobInvokerRegister;
    _functionRepository = functionRepository;
  }

  public void setNodePriority(final Integer nodePriority) {
    _nodePriority = nodePriority;
  }

  public Integer getNodePriority() {
    return _nodePriority;
  }

  protected JobInvokerRegister getJobInvokerRegister() {
    return _jobInvokerRegister;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage remoteCalcNodeMessage = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, message.getMessage());
    if (remoteCalcNodeMessage instanceof RemoteCalcNodeReadyMessage) {
      s_logger.info("Remote node connected - {}", connection);
      final FudgeSerializationContext scontext = new FudgeSerializationContext(fudgeContext);
      final RemoteCalcNodeInitMessage response = new RemoteCalcNodeInitMessage(getFunctionRepository());
      connection.getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(response), RemoteCalcNodeInitMessage.class, RemoteCalcNodeMessage.class));
      final RemoteNodeJobInvoker invoker = new RemoteNodeJobInvoker(getExecutorService(), (RemoteCalcNodeReadyMessage) remoteCalcNodeMessage, connection);
      if (getNodePriority() != null) {
        invoker.setNodePriority(getNodePriority());
      }
      getJobInvokerRegister().registerJobInvoker(invoker);
    } else {
      s_logger.warn("Unexpected message {}", remoteCalcNodeMessage);
    }
  }
}
