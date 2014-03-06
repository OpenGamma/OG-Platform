/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.ws.rs.core.Response.Status;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.livedata.rest.RemoteLiveDataInjector;
import com.opengamma.financial.rest.AbstractRestfulJmsResultConsumer;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.tuple.Pair;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link ViewClient}.
 * <p>
 * At most <b>one</b> remote view client is supported for any view client; attempting to attach more than one remote view client to a single engine-side view client may result in undesired behaviour
 * including inconsistencies.
 */
public class RemoteViewClient extends AbstractRestfulJmsResultConsumer<ViewResultListener> implements ViewClient {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewClient.class);
  /**
   * The view processor.
   */
  private final ViewProcessor _viewProcessor;
  /**
   * The lock for thread-safety.
   */
  private final ReentrantLock _listenerLock = new ReentrantLock();
  /**
   * The result listener.
   */
  private final ViewResultListener _internalResultListener;
  /**
   * The result listener.
   */
  private ViewResultListener _resultListener;
  /**
   * The latch for awaiting process completion.
   */
  private volatile CountDownLatch _completionLatch = new CountDownLatch(0);
  /**
   * The scheduler.
   */
  private final ScheduledExecutorService _scheduler;

  /**
   * Creates an instance.
   * 
   * @param viewProcessor the view processor
   * @param baseUri the base URI to connect to
   * @param fudgeContext the Fudge context
   * @param jmsConnector the JMS connector
   * @param scheduler the scheduler
   */
  public RemoteViewClient(ViewProcessor viewProcessor, URI baseUri, FudgeContext fudgeContext, JmsConnector jmsConnector, ScheduledExecutorService scheduler) {
    super(baseUri, fudgeContext, jmsConnector, scheduler, DataViewProcessorResource.VIEW_CLIENT_TIMEOUT_MILLIS / 2);
    _viewProcessor = viewProcessor;
    _scheduler = scheduler;
    _internalResultListener = new AbstractViewResultListener() {

      @Override
      public UserPrincipal getUser() {
        return RemoteViewClient.this.getUser();
      }

      @Override
      public void processCompleted() {
        RemoteViewClient.this.processCompleted();
      }

    };
  }

  //-------------------------------------------------------------------------
  @Override
  public void heartbeatFailed(Exception e) {
    super.heartbeatFailed(e);
    terminateRemoteClient();

    ViewResultListener listener = _resultListener;
    if (listener != null) {
      listener.clientShutdown(e);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_UNIQUE_ID);
    return getClient().accessFudge(uri).get(UniqueId.class);
  }

  @Override
  public UserPrincipal getUser() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_USER);
    return getClient().accessFudge(uri).get(UserPrincipal.class);
  }

  @Override
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  @Override
  public ViewClientState getState() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_STATE);
    return getClient().accessFudge(uri).get(ViewClientState.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAttached() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_IS_ATTACHED);
    return getClient().accessFudge(uri).get(Boolean.class);
  }

  @Override
  public void attachToViewProcess(UniqueId definitionId, ViewExecutionOptions executionOptions) {
    attachToViewProcess(definitionId, executionOptions, false);
  }

  @Override
  public void attachToViewProcess(UniqueId definitionId, ViewExecutionOptions executionOptions, boolean newBatchProcess) {
    AttachToViewProcessRequest request = new AttachToViewProcessRequest();
    request.setViewDefinitionId(definitionId);
    request.setExecutionOptions(executionOptions);
    request.setNewBatchProcess(newBatchProcess);
    _listenerLock.lock();
    try {
      _completionLatch = new CountDownLatch(1);
      URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_ATTACH_SEARCH);
      getClient().accessFudge(uri).post(request);
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public void attachToViewProcess(UniqueId processId) {
    _listenerLock.lock();
    try {
      _completionLatch = new CountDownLatch(1);
      URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_ATTACH_DIRECT);
      getClient().accessFudge(uri).post(processId);
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public void detachFromViewProcess() {
    _listenerLock.lock();
    try {
      URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_DETACH);
      getClient().accessFudge(uri).post();
      processCompleted();
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_LIVE_DATA_OVERRIDE_INJECTOR);
    return new RemoteLiveDataInjector(uri, getClient());
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_VIEW_DEFINITION);
    return getClient().accessFudge(uri).get(ViewDefinition.class);
  }

  @Override
  public ViewProcess getViewProcess() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_VIEW_PROCESS);
    return new RemoteViewProcess(uri, getClient());
  }
  
  private void processCompleted() {
    if (_completionLatch != null) {
      _completionLatch.countDown();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void setResultListener(ViewResultListener newListener) {
    _listenerLock.lock();
    final ViewResultListener oldListener = _resultListener;
    try {
      _resultListener = newListener;
      if (oldListener == null && newListener != null) {
        incrementListenerDemand();
      } else if (oldListener != null && newListener == null) {
        decrementListenerDemand();
      }
    } catch (JMSException e) {
      _resultListener = oldListener;
      throw new OpenGammaRuntimeException("JMS error configuring result listener", e);
    } catch (InterruptedException e) {
      _resultListener = oldListener;
      throw new OpenGammaRuntimeException("Interrupted before result listener was configured");
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public void setUpdatePeriod(long periodMillis) {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_UPDATE_PERIOD);
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(DataViewClientResource.UPDATE_PERIOD_FIELD, periodMillis);
    getClient().accessFudge(uri).put(msg);
  }

  @Override
  public void setViewProcessContextMap(Map<String, String> context) {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_VIEW_PROCESS_CONTEXT_MAP);
    getClient().accessFudge(uri).put(context);
  }

  @Override
  public ViewResultMode getResultMode() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_RESULT_MODE);
    return getClient().accessFudge(uri).get(ViewResultMode.class);
  }

  @Override
  public void setResultMode(ViewResultMode viewResultMode) {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_RESULT_MODE);
    getClient().accessFudge(uri).put(viewResultMode);
  }

  @Override
  public ViewResultMode getFragmentResultMode() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_FRAGMENT_RESULT_MODE);
    return getClient().accessFudge(uri).get(ViewResultMode.class);
  }

  @Override
  public void setFragmentResultMode(ViewResultMode viewResultMode) {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_FRAGMENT_RESULT_MODE);
    getClient().accessFudge(uri).put(viewResultMode);
  }

  //-------------------------------------------------------------------------
  @Override
  public void pause() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_PAUSE);
    getClient().accessFudge(uri).post();
  }

  @Override
  public void resume() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_RESUME);
    getClient().accessFudge(uri).post();
  }

  @Override
  public void triggerCycle() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_TRIGGER_CYCLE);
    getClient().accessFudge(uri).post();
  }

  @Override
  public void waitForCompletion() throws InterruptedException {
    _listenerLock.lock();
    try {
      incrementListenerDemand();
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("JMS error while setting up result listener", e);
    } finally {
      _listenerLock.unlock();
    }

    _completionLatch.await();

    _listenerLock.lock();
    try {
      decrementListenerDemand();
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("JMS error while removing result listener following completion", e);
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public boolean isResultAvailable() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_RESULT_AVAILABLE);
    return getClient().accessFudge(uri).get(Boolean.class);
  }

  @Override
  public boolean isCompleted() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_COMPLETED);
    return getClient().accessFudge(uri).get(Boolean.class);
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_LATEST_RESULT);
    try {
      return getClient().accessFudge(uri).get(ViewComputationResultModel.class);
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public CompiledViewDefinition getLatestCompiledViewDefinition() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_LATEST_COMPILED_VIEW_DEFINITION);
    try {
      return getClient().accessFudge(uri).get(CompiledViewDefinition.class);
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public boolean isViewCycleAccessSupported() {
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_VIEW_CYCLE_ACCESS_SUPPORTED);
    return getClient().accessFudge(uri).get(Boolean.class);
  }

  @Override
  public void setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(DataViewClientResource.VIEW_CYCLE_ACCESS_SUPPORTED_FIELD, isViewCycleAccessSupported);
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_VIEW_CYCLE_ACCESS_SUPPORTED);
    getClient().accessFudge(uri).post(msg);
  }

  @Override
  public EngineResourceReference<? extends ViewCycle> createCycleReference(UniqueId cycleId) {
    URI createReferenceUri = getUri(getBaseUri(), DataViewClientResource.PATH_CREATE_CYCLE_REFERENCE);
    ClientResponse response = getClient().accessFudge(createReferenceUri).post(ClientResponse.class, cycleId);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      return null;
    }
    URI referenceUri = response.getLocation();
    return new RemoteViewCycleReference(referenceUri, _scheduler, getClient());
  }

  @Override
  public RemoteEngineResourceReference<? extends ViewCycle> createLatestCycleReference() {
    URI createReferenceUri = getUri(getBaseUri(), DataViewClientResource.PATH_CREATE_LATEST_CYCLE_REFERENCE);
    ClientResponse response = getClient().accessFudge(createReferenceUri).post(ClientResponse.class);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      return null;
    }
    URI referenceUri = response.getLocation();
    return new RemoteViewCycleReference(referenceUri, _scheduler, getClient());
  }

  //-------------------------------------------------------------------------
  @Override
  public void setMinimumLogMode(ExecutionLogMode minimumLogMode, Set<Pair<String, ValueSpecification>> targets) {
    SetMinimumLogModeRequest request = new SetMinimumLogModeRequest();
    request.setMinimumLogMode(minimumLogMode);
    request.setTargets(targets);
    URI uri = getUri(getBaseUri(), DataViewClientResource.PATH_SET_MINIMUM_LOG_MODE);
    getClient().accessFudge(uri).post(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public void shutdown() {
    terminateRemoteClient();
    getClient().accessFudge(getBaseUri()).delete();
  }

  private void terminateRemoteClient() {
    stopHeartbeating();

    // Release any threads blocked on completion
    processCompleted();
  }

  //-------------------------------------------------------------------------
  @Override
  protected void dispatchListenerCall(Function<ViewResultListener, ?> listenerCall) {
    ViewResultListener listener = _resultListener;
    if (listener != null) {
      try {
        listenerCall.apply(listener);
      } catch (Exception e) {
        s_logger.warn("Exception notifying ViewClient listener of call " + listenerCall.getClass().getName(), e);
      }
    }
    listenerCall.apply(_internalResultListener);
  }

  @Override
  protected void onStartResultStream() {
    super.onStartResultStream();

    _completionLatch = new CountDownLatch(1);

    // We have not been listening to results so far, so initialise the state of the latch
    if (isAttached() && isCompleted()) {
      processCompleted();
    }
  }

  @Override
  protected void onEndResultStream() {
    super.onEndResultStream();
    _completionLatch = null;
  }

}
