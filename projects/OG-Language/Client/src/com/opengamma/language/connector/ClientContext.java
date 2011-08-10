/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.language.context.SessionContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents constant state held by most clients.
 */
public final class ClientContext {

  private final FudgeContext _fudgeContext;
  private final ScheduledExecutorService _scheduler;
  private final ClientExecutor _executor;
  private final int _messageTimeout;
  private final int _heartbeatTimeout;
  private final int _terminationTimeout;
  private final FudgeMsgEnvelope _heartbeatMessage;
  private final UserMessagePayloadVisitor<UserMessagePayload, SessionContext> _messageHandler;

  public ClientContext(final FudgeContext fudgeContext, final ScheduledExecutorService scheduler,
      final ClientExecutor executor, final int messageTimeout, final int heartbeatTimeout,
      final int terminationTimeout, final UserMessagePayloadVisitor<UserMessagePayload, SessionContext> messageHandler) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(scheduler, "scheduler");
    ArgumentChecker.notNull(executor, "executor");
    ArgumentChecker.notNull(messageHandler, "messageHandler");
    _fudgeContext = fudgeContext;
    _scheduler = scheduler;
    _executor = executor;
    _messageTimeout = messageTimeout;
    _heartbeatTimeout = heartbeatTimeout;
    _terminationTimeout = terminationTimeout;
    _heartbeatMessage = new FudgeMsgEnvelope(new ConnectorMessage(ConnectorMessage.Operation.HEARTBEAT)
        .toFudgeMsg(new FudgeSerializer(fudgeContext)), 0, MessageDirectives.CLIENT);
    _messageHandler = messageHandler;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  public ExecutorService createExecutor() {
    return _executor.createClientExecutor();
  }

  public int getMessageTimeout() {
    return _messageTimeout;
  }

  public int getHeartbeatTimeout() {
    return _heartbeatTimeout;
  }

  public int getTerminationTimeout() {
    return _terminationTimeout;
  }

  public FudgeMsgEnvelope getHeartbeatMessage() {
    return _heartbeatMessage;
  }

  public UserMessagePayloadVisitor<UserMessagePayload, SessionContext> getMessageHandler() {
    return _messageHandler;
  }

}
