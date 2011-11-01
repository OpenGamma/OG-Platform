/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.language.context.SessionContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Constructs a {@link ClientContext} instance
 */
public final class ClientContextFactoryBean implements ClientContextFactory, InitializingBean {

  /**
   * The Fudge context to use for encoding/decoding messages sent and received.
   */
  private FudgeContext _fudgeContext;

  /**
   * A scheduler to use for housekeeping tasks such as watchdogs.
   */
  private ScheduledExecutorService _housekeepingScheduler;

  /**
   * Message timeout. This should match the value used by the clients for consistent behavior. This is
   * typically used with the blocking message send routines as either its direct value or a multiple if
   * the operation is known to take an unusually long or short time. Messaging timeouts are typically
   * shorter than the heartbeat timeouts.
   */
  private int _messageTimeout;

  /**
   * Heartbeat timeout. This should match (or ideally be a touch less than) the value used by the clients.
   * If this is much lower the JVM process terminate prematurely. If this is much higher, the client
   * threads may run for longer than they need to after failure of the C++ process.
   */
  private int _heartbeatTimeout;

  /**
   * Termination timeout. This the time to wait for any threads spawned by a client to terminate.
   */
  private int _terminationTimeout;

  /**
   * Maximum number of threads per client.
   */
  private int _maxThreadsPerClient;

  /**
   * Maximum number of client threads in total.
   */
  private int _maxClientThreads;

  /**
   * Message handler.
   */
  private UserMessagePayloadVisitor<UserMessagePayload, SessionContext> _messageHandler;

  /**
   * The executor service provider. This is not set as part of the bean but constructed from the bean
   * attributes after they have all been set. This is exposed so that a language specific client
   * context can use the same executor so that the thread limits are enforced. If it were to create
   * its own executor then it would have its own pool of threads which may not be the intention.
   */
  private ClientExecutor _clientExecutor;

  public ClientContextFactoryBean() {
    setDefaults();
  }

  private void setDefaults() {
    setFudgeContext(FudgeContext.GLOBAL_DEFAULT);
    setHousekeepingScheduler(Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("Scheduler-")));
    setMessageTimeout(3000);
    setHeartbeatTimeout(4000);
    setTerminationTimeout(30000);
    setMaxThreadsPerClient(Math.max(2, Runtime.getRuntime().availableProcessors()));
    setMaxClientThreads(Integer.MAX_VALUE);
    // messageHandler defaults to null and must be set
  }

  public ClientContextFactoryBean(final ClientContextFactoryBean copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    setFudgeContext(copyFrom.getFudgeContext());
    setHousekeepingScheduler(copyFrom.getHousekeepingScheduler());
    setMessageTimeout(copyFrom.getMessageTimeout());
    setHeartbeatTimeout(copyFrom.getHeartbeatTimeout());
    setTerminationTimeout(copyFrom.getTerminationTimeout());
    setMaxThreadsPerClient(copyFrom.getMaxThreadsPerClient());
    setMaxClientThreads(copyFrom.getMaxClientThreads());
    setMessageHandler(copyFrom.getMessageHandler());
    setClientExecutor(copyFrom.getClientExecutor());
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setHousekeepingScheduler(final ScheduledExecutorService housekeepingScheduler) {
    ArgumentChecker.notNull(housekeepingScheduler, "housekeepingScheduler");
    _housekeepingScheduler = housekeepingScheduler;
  }

  public ScheduledExecutorService getHousekeepingScheduler() {
    return _housekeepingScheduler;
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

  public void setMaxThreadsPerClient(final int maxThreadsPerClient) {
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

  // Non spring-settable part of the bean - the internal ClientExecutor is created after properties have been set. This
  // is provided so that an existing factory bean can be hijacked 

  public void setClientExecutor(final ClientExecutor clientExecutor) {
    ArgumentChecker.notNull(clientExecutor, "clientExecutor");
    _clientExecutor = clientExecutor;
    setMaxThreadsPerClient(clientExecutor.getMaxThreadsPerClient());
    setMaxClientThreads(clientExecutor.getMaxThreads());
  }

  public ClientExecutor getClientExecutor() {
    return _clientExecutor;
  }

  @Override
  public void afterPropertiesSet() {
    // Only messageHandler could still be null - the others have defaults and won't let null be set
    ArgumentChecker.notNull(getMessageHandler(), "messageHandler");
    _clientExecutor = new ClientExecutor(getMaxThreadsPerClient(), getMaxClientThreads());
  }

  // ClientContextFactory

  @Override
  public ClientContext createClientContext() {
    return new ClientContext(getFudgeContext(), getHousekeepingScheduler(), getClientExecutor(), getMessageTimeout(), getHeartbeatTimeout(), getTerminationTimeout(), getMessageHandler());
  }

}
