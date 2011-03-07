/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.language.context.SessionContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Constructs {@link Client} instances as connections are received.
 */
public final class ClientFactoryBean implements InitializingBean, Lifecycle {

  /**
   * The Fudge context to use for encoding/decoding messages sent and received.
   */
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

  /**
   * A scheduler to use for housekeeping tasks such as watchdogs.
   */
  private ScheduledExecutorService _scheduler = Executors
      .newSingleThreadScheduledExecutor(new CustomizableThreadFactory("Scheduler-"));

  /**
   * Message timeout. This should match the value used by the clients for consistent behavior. This is
   * typically used with the blocking message send routines as either its direct value or a multiple if
   * the operation is known to take an unusually long or short time. Messaging timeouts are typically
   * shorter than the heartbeat timeouts.
   */
  private int _messageTimeout = 3000;

  /**
   * Heartbeat timeout. This should match (or ideally be a touch less than) the value used by the clients.
   * If this is much lower the JVM process terminate prematurely. If this is much higher, the client
   * threads may run for longer than they need to after failure of the C++ process.
   */
  private int _heartbeatTimeout = 4000;

  /**
   * Termination timeout. This the time to wait for any threads spawned by a client to terminate.
   */
  private int _terminationTimeout = 30000;

  /**
   * Maximum number of threads per client.
   */
  private int _maxThreadsPerClient = Math.max(2, Runtime.getRuntime().availableProcessors());

  /**
   * Maximum number of client threads in total.
   */
  private int _maxClientThreads = Integer.MAX_VALUE;

  /**
   * Message handler.
   */
  private UserMessagePayloadVisitor<UserMessagePayload, SessionContext> _messageHandler;

  private ClientContext _clientContext;

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setScheduler(final ScheduledExecutorService scheduler) {
    ArgumentChecker.notNull(scheduler, "scheduler");
    _scheduler = scheduler;
  }

  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  public void setMessageTimeout(final int messageTimeout) {
    ArgumentChecker.notNegativeOrZero(messageTimeout, "messageTimeout");
    _messageTimeout = messageTimeout;
  }

  public int getMessageTimeout() {
    return _messageTimeout;
  }

  public void setHeartbeatTimeout(final int heartbeatTimeout) {
    ArgumentChecker.notNegativeOrZero(heartbeatTimeout, "heartbeatTimeout");
    _heartbeatTimeout = heartbeatTimeout;
  }

  public int getHeartbeatTimeout() {
    return _heartbeatTimeout;
  }

  public void setTerminationTimeout(final int terminationTimeout) {
    ArgumentChecker.notNegativeOrZero(terminationTimeout, "terminationTimeout");
    _terminationTimeout = terminationTimeout;
  }

  public int getTerminationTimeout() {
    return _terminationTimeout;
  }

  public void setMaxThreadPerClient(final int maxThreadsPerClient) {
    _maxThreadsPerClient = maxThreadsPerClient;
  }

  public int getMaxThreadsPerClient() {
    return _maxThreadsPerClient;
  }

  public void setMaxClientThreads(final int maxClientThreads) {
    _maxClientThreads = maxClientThreads;
  }

  public int getMaxClientThreads() {
    return _maxClientThreads;
  }

  public void setMessageHandler(final UserMessagePayloadVisitor<UserMessagePayload, SessionContext> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    _messageHandler = visitor;
  }

  public UserMessagePayloadVisitor<UserMessagePayload, SessionContext> getMessageHandler() {
    return _messageHandler;
  }

  private void setClientContext(final ClientContext context) {
    _clientContext = context;
  }

  private ClientContext getClientContext() {
    return _clientContext;
  }

  public Client createClient(final String inputPipeName, final String outputPipeName,
      final SessionContext sessionContext) {
    return new Client(getClientContext(), inputPipeName, outputPipeName, sessionContext);
  }

  // Lifecycle

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public void start() {
    setClientContext(new ClientContext(getFudgeContext(), getScheduler(), new ClientExecutor(getMaxThreadsPerClient(),
        getMaxClientThreads()), getMessageTimeout(), getHeartbeatTimeout(), getTerminationTimeout(),
        getMessageHandler()));
  }

  @Override
  public void stop() {
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getMessageHandler(), "messageHandler");
  }

}
