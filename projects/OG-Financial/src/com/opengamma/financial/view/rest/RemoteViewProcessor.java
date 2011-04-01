/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import javax.jms.ConnectionFactory;
import javax.ws.rs.core.UriBuilder;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.ViewCycleManager;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link ViewProcessor}.
 */
public class RemoteViewProcessor implements ViewProcessor {

  private final URI _baseUri;
  private final ScheduledExecutorService _scheduler;
  private final FudgeRestClient _client;
  private final JmsTemplate _jmsTemplate;
  
  public RemoteViewProcessor(URI baseUri, ConnectionFactory connectionFactory, ScheduledExecutorService scheduler) {
    _baseUri = baseUri;
    _scheduler = scheduler;
    _client = FudgeRestClient.create();
    
    _jmsTemplate = new JmsTemplate();
    _jmsTemplate.setConnectionFactory(connectionFactory);
    _jmsTemplate.setPubSubDomain(true);
  }

  @Override
  public UniqueIdentifier getUniqueId() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_UNIQUE_ID).build();
    return _client.access(uri).get(UniqueIdentifier.class);
  }

  @Override
  public ViewDefinitionRepository getViewDefinitionRepository() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_DEFINITION_REPOSITORY).build();
    return new RemoteViewDefinitionRepository(uri);
  }

  //-------------------------------------------------------------------------
  @Override
  public ViewProcess getViewProcess(UniqueIdentifier viewProcessId) {
    URI uri = DataViewProcessorResource.uriViewProcess(_baseUri, viewProcessId);
    return new RemoteViewProcess(uri);
  }

  //-------------------------------------------------------------------------
  @Override
  public ViewClient createViewClient(UserPrincipal clientUser) {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CLIENTS).build();
    ClientResponse response = _client.access(uri).post(ClientResponse.class, clientUser);
    URI clientLocation = response.getLocation();
    return new RemoteViewClient(this, clientLocation, OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }

  @Override
  public ViewClient getViewClient(UniqueIdentifier clientId) {
    URI clientUri = DataViewProcessorResource.uriClient(_baseUri, clientId);
    return new RemoteViewClient(this, clientUri, OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }

  //-------------------------------------------------------------------------
  @Override
  public ViewCycleManager getViewCycleManager() {
    URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessorResource.PATH_CYCLES).build();
    return new RemoteViewCycleManager(uri, _scheduler);
  }

}
