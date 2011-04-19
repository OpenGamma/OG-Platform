/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.google.common.base.Function;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.livedata.rest.RemoteLiveDataInjector;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link ViewClient}.
 * <p>
 * At most <b>one</b> remote view client is supported for any view client; attempting to attach more than one remote
 * view client to a single engine-side view client may result in undesired behaviour including inconsistencies.
 */
public class RemoteViewClient implements ViewClient {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewClient.class);
  
  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final ViewProcessor _viewProcessor;
  
  private final ReentrantLock _listenerLock = new ReentrantLock();
  private final ViewResultListener _internalResultListener;
  private long _listenerDemand;
  private ViewResultListener _resultListener;
  private DefaultMessageListenerContainer _resultListenerContainer;
  private volatile CountDownLatch _completionLatch = new CountDownLatch(0);
  
  private final FudgeContext _fudgeContext;
  private final JmsTemplate _jmsTemplate;
  private final ScheduledExecutorService _scheduler;
  
  public RemoteViewClient(ViewProcessor viewProcessor, URI baseUri, FudgeContext fudgeContext, JmsTemplate jmsTemplate, ScheduledExecutorService scheduler) {
    _viewProcessor = viewProcessor;
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _fudgeContext = fudgeContext;
    _jmsTemplate = jmsTemplate;
    _scheduler = scheduler;
    
    _internalResultListener = new AbstractViewResultListener() {

      @Override
      public void processCompleted() {
        RemoteViewClient.this.processCompleted();
      }
      
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier getUniqueId() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_UNIQUE_ID);
    return _client.access(uri).get(UniqueIdentifier.class);
  }
  
  @Override
  public UserPrincipal getUser() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_USER);
    return _client.access(uri).get(UserPrincipal.class);
  }
  
  @Override
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  @Override
  public ViewClientState getState() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_STATE);
    return _client.access(uri).get(ViewClientState.class);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean isAttached() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_IS_ATTACHED);
    return _client.access(uri).get(Boolean.class);
  }
  
  @Override
  public void attachToViewProcess(String viewDefinitionName, ViewExecutionOptions executionOptions) {
    attachToViewProcess(viewDefinitionName, executionOptions, false);
  }

  @Override
  public void attachToViewProcess(String viewDefinitionName, ViewExecutionOptions executionOptions, boolean newBatchProcess) {
    AttachToViewProcessRequest request = new AttachToViewProcessRequest();
    request.setViewDefinitionName(viewDefinitionName);
    request.setExecutionOptions(executionOptions);
    request.setNewBatchProcess(newBatchProcess);
    _listenerLock.lock();
    try {
      _completionLatch = new CountDownLatch(1);
      URI uri = getUri(_baseUri, DataViewClientResource.PATH_ATTACH_SEARCH);
      _client.access(uri).post(request);
    } finally {
      _listenerLock.unlock();
    }
  }

  @Override
  public void attachToViewProcess(UniqueIdentifier processId) {
    _listenerLock.lock();
    try {
      _completionLatch = new CountDownLatch(1);
      URI uri = getUri(_baseUri, DataViewClientResource.PATH_ATTACH_DIRECT);
      _client.access(uri).post(processId);
    } finally {
      _listenerLock.unlock();
    }
  }
  
  @Override
  public void detachFromViewProcess() {
    _listenerLock.lock();
    try {
      URI uri = getUri(_baseUri, DataViewClientResource.PATH_DETACH);
      _client.access(uri).post();
      processCompleted();
    } finally {
      _listenerLock.unlock();
    }
  }
  
  @Override
  public LiveDataInjector getLiveDataOverrideInjector() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_LIVE_DATA_OVERRIDE_INJECTOR);
    return new RemoteLiveDataInjector(uri);
  }
  
  private void processCompleted() {
    _completionLatch.countDown();
  }

  //-------------------------------------------------------------------------
  @Override
  public void setResultListener(ViewResultListener newListener) {
    _listenerLock.lock();
    try {
      ViewResultListener oldListener = _resultListener;
      _resultListener = newListener;
      if (oldListener == null && newListener != null) {
        _listenerDemand++;
      } else if (oldListener != null && newListener == null) {
        _listenerDemand--;
      }
      configureResultListener();
    } finally {
      _listenerLock.unlock();
    }
  }
  
  private void configureResultListener() {
    if (_listenerDemand == 0) {
      URI uri = getUri(_baseUri, DataViewClientResource.PATH_STOP_JMS_RESULT_STREAM);
      _client.access(uri).post();
      tearDownResultListener();
      _completionLatch = null;
    } else if (_listenerDemand == 1) {
      _completionLatch = new CountDownLatch(1);
      
      URI uri = getUri(_baseUri, DataViewClientResource.PATH_START_JMS_RESULT_STREAM);
      String destinationName = _client.access(uri).post(String.class);
      initResultListener(destinationName);
      
      // We have not been listening to results so far, so initialise the state of the latch
      if (isAttached() && isCompleted()) {
        _completionLatch.countDown();
      }
    }
  }
  
  private void initResultListener(final String destinationName) {
    s_logger.info("Set up result JMS subscription to {}", destinationName);
    _resultListenerContainer = new DefaultMessageListenerContainer();
    _resultListenerContainer.setConnectionFactory(_jmsTemplate.getConnectionFactory());
    _resultListenerContainer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
      @SuppressWarnings("unchecked")
      @Override
      public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
        s_logger.debug("Result listener call received on {}", destinationName);
        Function<ViewResultListener, ?> listenerCall;
        try {
          listenerCall = fudgeContext.fromFudgeMsg(Function.class, msgEnvelope.getMessage());
        } catch (Exception e) {
          s_logger.warn("Caught exception parsing message", e);
          s_logger.debug("Couldn't parse message {}", msgEnvelope.getMessage());
          return;
        }
        dispatchListenerCall(listenerCall);
      }
    }, _fudgeContext)));
    _resultListenerContainer.setDestinationName(destinationName);
    _resultListenerContainer.setPubSubDomain(true);
    _resultListenerContainer.setExceptionListener(new ExceptionListener() {
      @Override
      public void onException(JMSException exception) {
        s_logger.warn("Error in listener call receiver", exception);
      }
    });
    _resultListenerContainer.afterPropertiesSet();
    _resultListenerContainer.start();
  }
  
  private void dispatchListenerCall(Function<ViewResultListener, ?> listenerCall) {
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
  
  private void tearDownResultListener() {
    _resultListenerContainer.stop();
    _resultListenerContainer.destroy();
    _resultListenerContainer = null;
  }

  @Override
  public void setUpdatePeriod(long periodMillis) {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_UPDATE_PERIOD);
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(DataViewClientResource.UPDATE_PERIOD_FIELD, periodMillis);
    _client.access(uri).put(msg);
  }
  
  @Override
  public ViewResultMode getResultMode() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_RESULT_MODE);
    return _client.access(uri).get(ViewResultMode.class);
  }

  @Override
  public void setResultMode(ViewResultMode viewResultMode) {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_RESULT_MODE);
    _client.access(uri).put(viewResultMode);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void pause() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_PAUSE);
    _client.access(uri).post();
  }
  
  @Override
  public void resume() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_RESUME);
    _client.access(uri).post();
  }
  
  @Override
  public void waitForCompletion() throws InterruptedException {
    _listenerLock.lock();
    try {
      _listenerDemand++;
      configureResultListener();
    } finally {
      _listenerLock.unlock();
    }
    
    _completionLatch.await();
    
    _listenerLock.lock();
    try {
      _listenerDemand--;
      configureResultListener();
    } finally {
      _listenerLock.unlock();
    }
  }
  
  @Override
  public boolean isResultAvailable() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_RESULT_AVAILABLE);
    return _client.access(uri).get(Boolean.class);
  }
  
  @Override
  public boolean isCompleted() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_COMPLETED);
    return _client.access(uri).get(Boolean.class);
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_LATEST_RESULT);
    return _client.access(uri).get(ViewComputationResultModel.class);
  }
  
  @Override
  public CompiledViewDefinition getLatestCompiledViewDefinition() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_LATEST_COMPILED_VIEW_DEFINITION);
    return _client.access(uri).get(CompiledViewDefinition.class);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean isViewCycleAccessSupported() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_VIEW_CYCLE_ACCESS_SUPPORTED);
    return _client.access(uri).get(Boolean.class);
  }
  
  @Override
  public void setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(DataViewClientResource.VIEW_CYCLE_ACCESS_SUPPORTED_FIELD, isViewCycleAccessSupported);
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_VIEW_CYCLE_ACCESS_SUPPORTED);
    _client.access(uri).post(msg);
  }
  
  @Override
  public EngineResourceReference<? extends ViewCycle> createCycleReference(UniqueIdentifier cycleId) {
    URI createReferenceUri = getUri(_baseUri, DataViewClientResource.PATH_CREATE_CYCLE_REFERENCE);
    ClientResponse response = _client.access(createReferenceUri).post(ClientResponse.class, cycleId);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      return null;
    }
    URI referenceUri = response.getLocation();
    return new RemoteViewCycleReference(referenceUri, _scheduler);
  }
  
  @Override
  public RemoteEngineResourceReference<? extends ViewCycle> createLatestCycleReference() {
    URI createReferenceUri = getUri(_baseUri, DataViewClientResource.PATH_CREATE_LATEST_CYCLE_REFERENCE);
    ClientResponse response = _client.access(createReferenceUri).post(ClientResponse.class);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      return null;
    }
    URI referenceUri = response.getLocation();
    return new RemoteViewCycleReference(referenceUri, _scheduler);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void shutdown() {
    URI uri = getUri(_baseUri, DataViewClientResource.PATH_SHUTDOWN);
    _client.access(uri).post();
  }

  //-------------------------------------------------------------------------
  private static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }
  
}
