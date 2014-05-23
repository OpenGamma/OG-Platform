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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
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
   * @param baseUri the base URI of the remote view processor
   * @param jmsConnector the JMS connector
   * @param heartbeatScheduler the scheduler to be used to send heartbeats to the remote view processor
   */
  public RemoteViewProcessor(URI baseUri, JmsConnector jmsConnector, ScheduledExecutorService heartbeatScheduler) {
    _baseUri = baseUri;
    _heartbeatScheduler = heartbeatScheduler;
    _client = FudgeRestClient.create();
    _jmsConnector = jmsConnector;
  }

  @Override
  public String getName() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_NAME).build();
    return _client.accessFudge(uri).get(String.class);
  }

  @Override
  public ConfigSource getConfigSource() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CONFIG_SOURCE).build();
    return new RemoteConfigSource(uri);
  }

  @Override
  public NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_NAMED_MARKET_DATA_SPEC_REPOSITORY).build();
    return new RemoteNamedMarketDataSpecificationRepository(uri);
  }

  //-------------------------------------------------------------------------
  @Override
  public ViewProcess getViewProcess(UniqueId viewProcessId) {
    URI uri = DataViewProcessorResource.uriViewProcess(_baseUri, viewProcessId);
    return new RemoteViewProcess(uri, _client);
  }

  //-------------------------------------------------------------------------
  @Override
  public RemoteViewClient createViewClient(UserPrincipal clientUser) {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CLIENTS).build();
    ClientResponse response = _client.accessFudge(uri).post(ClientResponse.class, clientUser);
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
    return new RemoteViewCycleManager(uri, _heartbeatScheduler, _client);
  }

  //-------------------------------------------------------------------------
  public MarketDataSnapshotter getMarketDataSnapshotter(MarketDataSnapshotter.Mode mode) {
    URI uri = DataViewProcessorResource.uriSnapshotter(_baseUri, mode);
    return new RemoteMarketDataSnapshotter(uri, _client);
  }
}
