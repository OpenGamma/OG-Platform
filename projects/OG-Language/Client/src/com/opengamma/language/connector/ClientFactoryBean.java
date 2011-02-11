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
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Constructs {@link Client} instances as connections are received.
 */
public final class ClientFactoryBean implements InitializingBean {

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

  private Client.Context _clientContext;

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

  private void setClientContext(final Client.Context context) {
    _clientContext = context;
  }

  private Client.Context getClientContext() {
    return _clientContext;
  }

  public Client createClient(final String inputPipeName, final String outputPipeName) {
    return new Client(getClientContext(), inputPipeName, outputPipeName);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setClientContext(new Client.Context(getFudgeContext(), getScheduler(), new ClientExecutor(getMaxThreadsPerClient(),
        getMaxClientThreads()), getHeartbeatTimeout(), getTerminationTimeout()));
  }

}
