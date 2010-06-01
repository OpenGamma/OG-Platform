/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_ALLSECURITYTYPES;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_ALLVALUENAMES;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_COMPUTATIONRESULT;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_DELTARESULT;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_LIVECOMPUTATIONRUNNING;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_MOSTRECENTRESULT;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_PERFORMCOMPUTATION;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_PORTFOLIO;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_REQUIREMENTNAMES;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_RESULTAVAILABLE;
import static com.opengamma.engine.view.server.ViewProcessorServiceNames.VIEW_STATUS;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of a ViewClient for working with a remote engine. The RemoteViewProcessorClient will only create
 * a single instance of RemoteViewClient for each remote View.
 *
 */
/* package */ class RemoteViewClient implements ViewClient {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewClient.class);
  
  private final RemoteViewProcessorClient _viewProcessorClient;
  
  private final String _name;
  private final UserPrincipal _user;
  
  private final RestTarget _targetAllSecurityTypes;
  private final RestTarget _targetAllValueNames;
  private final RestTarget _targetMostRecentResult;
  private final RestTarget _targetPortfolio;
  private final RestTarget _targetRequirementNames;
  private final RestTarget _targetStatus;
  private final RestTarget _targetPerformComputation;
  private final RestTarget _targetComputationResult;
  private final RestTarget _targetDeltaResult;
  
  private final Set<ComputationResultListener> _resultListeners = new CopyOnWriteArraySet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new CopyOnWriteArraySet<DeltaComputationResultListener>();
  
  private DefaultMessageListenerContainer _resultListenerContainer;
  private DefaultMessageListenerContainer _deltaListenerContainer;
  
  protected RemoteViewClient(final RemoteViewProcessorClient viewProcessorClient, 
      final String name, 
      final RestTarget target,
      final UserPrincipal user) {
    _viewProcessorClient = viewProcessorClient;
    _name = name;
    _targetAllSecurityTypes = target.resolve(VIEW_ALLSECURITYTYPES);
    _targetAllValueNames = target.resolve(VIEW_ALLVALUENAMES);
    _targetMostRecentResult = target.resolve(VIEW_MOSTRECENTRESULT);
    _targetPortfolio = target.resolve(VIEW_PORTFOLIO);
    _targetRequirementNames = target.resolve(VIEW_REQUIREMENTNAMES);
    _targetStatus = target.resolve(VIEW_STATUS);
    _targetPerformComputation = target.resolve(VIEW_PERFORMCOMPUTATION);
    _targetComputationResult = target.resolve(VIEW_COMPUTATIONRESULT);
    _targetDeltaResult = target.resolve(VIEW_DELTARESULT);
    _user = user;
  }
  
  protected RemoteViewProcessorClient getViewProcessorClient() {
    return _viewProcessorClient;
  }
  
  protected FudgeContext getFudgeContext() {
    return getViewProcessorClient().getFudgeContext();
  }
  
  protected RestClient getRestClient() {
    return getViewProcessorClient().getRestClient();
  }
  
  // TODO 2010-03-30 Andrew -- needs to detect failure of the server (e.g. a lack of messages for a timeout) and reconnect by re-requesting the URL.
  // Failure of the MOM and/or the other server can be recovered from that way. 
  
  @Override
  public void addComputationResultListener(ComputationResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    synchronized (_resultListeners) {
      if (_resultListeners.isEmpty()) {
        final String topicName = getRestClient().getSingleValueNotNull(String.class, _targetComputationResult, VIEW_COMPUTATIONRESULT);
        s_logger.info("Set up JMS subscription to {}", topicName);
        _resultListenerContainer = new DefaultMessageListenerContainer();
        _resultListenerContainer.setConnectionFactory(getViewProcessorClient().getJmsTemplate().getConnectionFactory());
        _resultListenerContainer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
          @Override
          public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
            s_logger.debug("Message received on {}", topicName);
            dispatchComputationResult(fudgeContext.fromFudgeMsg(ViewComputationResultModel.class, msgEnvelope.getMessage()));
          }
        }, getFudgeContext())));
        _resultListenerContainer.setDestinationName(topicName);
        _resultListenerContainer.setPubSubDomain(true);
        _resultListenerContainer.afterPropertiesSet();
        _resultListenerContainer.start();
      }
      _resultListeners.add(listener);
    }
  }

  @Override
  public void addDeltaResultListener(DeltaComputationResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    synchronized (_deltaListeners) {
      if (_deltaListeners.isEmpty()) {
        final String topicName = getRestClient().getSingleValueNotNull(String.class, _targetDeltaResult, VIEW_DELTARESULT);
        s_logger.info("Set up JMS subscription to {}", topicName);
        _deltaListenerContainer = new DefaultMessageListenerContainer();
        _deltaListenerContainer.setConnectionFactory(getViewProcessorClient().getJmsTemplate().getConnectionFactory());
        _deltaListenerContainer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
          @Override
          public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
            s_logger.debug("Message received on {}", topicName);
            dispatchDeltaResult(fudgeContext.fromFudgeMsg(ViewDeltaResultModel.class, msgEnvelope.getMessage()));
          }
        }, getFudgeContext())));
        _deltaListenerContainer.setDestinationName(topicName);
        _deltaListenerContainer.setPubSubDomain(true);
        _deltaListenerContainer.afterPropertiesSet();
        _deltaListenerContainer.start();
      }
      _deltaListeners.add(listener);
    }
  }

  @Override
  public void removeComputationResultListener(ComputationResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    synchronized (_resultListeners) {
      _resultListeners.remove(listener);
      if (_resultListeners.isEmpty()) {
        s_logger.info("Stopping JMS subscription");
        _resultListenerContainer.stop();
        _resultListenerContainer.destroy();
        _resultListenerContainer = null;
      }
    }
  }

  @Override
  public void removeDeltaResultListener(DeltaComputationResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    synchronized (_deltaListeners) {
      _deltaListeners.remove(listener);
      if (_deltaListeners.isEmpty()) {
        s_logger.info("Stopping JMS subscription");
        _deltaListenerContainer.stop();
        _deltaListenerContainer.destroy();
        _deltaListenerContainer = null;
      }
    }
  }
  
  protected void dispatchComputationResult(ViewComputationResultModel resultModel) {
    for (ComputationResultListener listener : _resultListeners) {
      listener.computationResultAvailable(resultModel);
    }
  }
  
  protected void dispatchDeltaResult(ViewDeltaResultModel deltaModel) {
    for (DeltaComputationResultListener listener : _deltaListeners) {
      listener.deltaResultAvailable(deltaModel);
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllSecurityTypes() {
    return getRestClient().getSingleValueNotNull(Set.class, _targetAllSecurityTypes, VIEW_ALLSECURITYTYPES);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllValueNames() {
    return getRestClient().getSingleValueNotNull(Set.class, _targetAllValueNames, VIEW_ALLVALUENAMES);
  }

  @Override
  public ViewComputationResultModel getMostRecentResult() {
    return getRestClient().getSingleValue(ViewComputationResultModel.class, _targetMostRecentResult, VIEW_MOSTRECENTRESULT);
  }
  
  @Override
  public String getName() {
    return _name;
  }
  
  @Override
  public UserPrincipal getUser() {
    return _user;
  }

  @Override
  public Portfolio getPortfolio() {
    return getRestClient().getSingleValueNotNull(Portfolio.class, _targetPortfolio, VIEW_PORTFOLIO);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getRequirementNames(String securityType) {
    return getRestClient().getSingleValueNotNull(Set.class, _targetRequirementNames.resolve(securityType), VIEW_REQUIREMENTNAMES);
  }

  @Override
  public boolean isLiveComputationRunning() {
    return getRestClient().getSingleValueNotNull(Boolean.class, _targetStatus, VIEW_LIVECOMPUTATIONRUNNING);
  }

  @Override
  public boolean isResultAvailable() {
    return getRestClient().getSingleValueNotNull(Boolean.class, _targetStatus, VIEW_RESULTAVAILABLE);
  }

  @Override
  public void performComputation() {
    getRestClient().post(_targetPerformComputation);
  }
  
  @Override
  public void performComputation(long snapshotTime) {
    getRestClient().post(_targetPerformComputation.resolve(Long.toString(snapshotTime)));
  }

}
