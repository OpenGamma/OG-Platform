/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link ViewProcessor}.
 */
public class RemoteViewProcessor implements ViewProcessor {

  private final URI _baseUri;
  private final ScheduledExecutorService _heartbeatScheduler;
  private final FudgeRestClient _client;
  private final JmsConnector _jmsConnector;
  
  /**
   * Constructs an instance.
   * 
   * @param baseUri  the base URI of the remote view processor
   * @param jmsConnector  the JMS connector
   * @param heartbeatScheduler  the scheduler to be used to send heartbeats to the remote view processor
   */
  public RemoteViewProcessor(URI baseUri, JmsConnector jmsConnector, ScheduledExecutorService heartbeatScheduler) {
    _baseUri = baseUri;
    _heartbeatScheduler = heartbeatScheduler;
    _client = FudgeRestClient.create();
    _jmsConnector = jmsConnector;
  }

  @Override
  public UniqueId getUniqueId() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_UNIQUE_ID).build();
    return _client.access(uri).get(UniqueId.class);
  }

  @Override
  public ViewDefinitionRepository getViewDefinitionRepository() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_DEFINITION_REPOSITORY).build();
    return new RemoteViewDefinitionRepository(uri);
  }

  @Override
  public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_LIVE_DATA_SOURCE_REGISTRY).build();
    return new RemoteLiveMarketDataSourceRegistry(uri);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ViewProcess getViewProcess(UniqueId viewProcessId) {
    URI uri = DataViewProcessorResource.uriViewProcess(_baseUri, viewProcessId);
    return new RemoteViewProcess(uri);
  }

  //-------------------------------------------------------------------------
  @Override
  public RemoteViewClient createViewClient(UserPrincipal clientUser) {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CLIENTS).build();
    ClientResponse response = _client.access(uri).post(ClientResponse.class, clientUser);
    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      throw new OpenGammaRuntimeException("Could not create view client: " + response);
    }
    URI clientLocation = response.getLocation();
    return new RemoteViewClient(this, clientLocation, OpenGammaFudgeContext.getInstance(), _jmsConnector, _heartbeatScheduler);
  }

  @Override
  public ViewClient getViewClient(UniqueId clientId) {
    URI clientsBaseUri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CLIENTS).build();
    URI clientUri = DataViewProcessorResource.uriClient(clientsBaseUri, clientId);
    return new RemoteViewClient(this, clientUri, OpenGammaFudgeContext.getInstance(), _jmsConnector, _heartbeatScheduler);
  }

  //-------------------------------------------------------------------------
  @Override
  public EngineResourceManager<ViewCycle> getViewCycleManager() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CYCLES).build();
    return new RemoteViewCycleManager(uri, _heartbeatScheduler);
  }

}
