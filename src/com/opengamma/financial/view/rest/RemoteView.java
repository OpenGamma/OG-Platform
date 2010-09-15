/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.livedata.rest.RemoteLiveDataInjector;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link View}.
 */
public class RemoteView implements View {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final JmsTemplate _jmsTemplate;
  
  public RemoteView(URI baseUri, JmsTemplate jmsTemplate) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _jmsTemplate = jmsTemplate;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_NAME);
    return _client.access(uri).get(String.class);
  }
  
  @Override
  public ViewDefinition getDefinition() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_DEFINITION);
    return _client.access(uri).get(ViewDefinition.class);
  }
  
  @Override
  public void init() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_INIT);
    _client.access(uri).post();
  }
  
  @Override
  public Portfolio getPortfolio() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_PORTFOLIO);
    return _client.access(uri).get(Portfolio.class);
  }
  
  @Override
  public ViewClient createClient(UserPrincipal credentials) {
    UniqueIdentifier uid = _client.access(DataViewResource.uriClients(_baseUri)).post(UniqueIdentifier.class, credentials);
    return new RemoteViewClient(this, DataViewResource.uriClient(_baseUri, uid), OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }
  
  @Override
  public ViewClient getClient(UniqueIdentifier id) {
    URI clientUri = DataViewResource.uriClient(_baseUri, id);
    return new RemoteViewClient(this, clientUri, OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }
  
  @Override
  public void assertAccessToLiveDataRequirements(UserPrincipal user) {
    URI uri = getUri(_baseUri, DataViewResource.PATH_ACCESS_TO_LIVE_DATA);
    _client.access(uri)
        .queryParam(DataViewResource.PARAM_USER_IP, user.getIpAddress())
        .queryParam(DataViewResource.PARAM_USER_NAME, user.getUserName()).post();
  }
  
  @Override
  public ViewComputationResultModel getLatestResult() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LATEST_RESULT);
    return _client.access(uri).get(ViewComputationResultModel.class);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_REQUIRED_LIVE_DATA);
    return _client.access(uri).get(Set.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllSecurityTypes() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_ALL_SECURITY_TYPES);
    return _client.access(uri).get(Set.class);
  }
  
  @Override
  public void runOneCycle() {
    // TODO: implement
  }
  
  @Override
  public void runOneCycle(long valuationTime) {
    // TODO: implement
  }
  
  @Override
  public boolean isLiveComputationRunning() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LIVE_COMPUTATION_RUNNING);
    return _client.access(uri).get(boolean.class);
  }

  @Override
  public LiveDataInjector getLiveDataInjector() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LIVE_DATA_INJECTOR);
    return new RemoteLiveDataInjector(uri);
  }
  
  //-------------------------------------------------------------------------
  private static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).build(path);
  }

}
