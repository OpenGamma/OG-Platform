/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.ws.rs.core.UriBuilder;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Provides access to a remote {@link ViewProcessor}.
 */
public class RemoteViewProcessor implements ViewProcessor {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final JmsTemplate _jmsTemplate;
  
  public RemoteViewProcessor(URI baseUri, ConnectionFactory connectionFactory) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    
    _jmsTemplate = new JmsTemplate();
    _jmsTemplate.setConnectionFactory(connectionFactory);
    _jmsTemplate.setPubSubDomain(true);
  }
  
  @Override
  public View getView(String name, UserPrincipal credentials) {
    View view = new RemoteView(DataViewProcessorResource.uriView(_baseUri, name), _jmsTemplate);
    try {
      // Attempt to access something lightweight on the view to check it exists 
      view.getName();
    } catch (UniformInterfaceException e) {
      if (e.getResponse().getStatus() == 404) {
        // The method should return null if the view doesn't exist, not throw an exception
        return null;
      }
      throw e;
    }
    return view;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getViewNames() {
    URI uri = getUri(_baseUri, DataViewProcessorResource.PATH_VIEW_NAMES);
    return _client.access(uri).get(Set.class);
  }
  
  @Override
  public void reinitAsync() {
    URI uri = getUri(_baseUri, DataViewProcessorResource.PATH_REINIT_ASYNC);
    _client.access(uri).post();
  }
  
  //-------------------------------------------------------------------------
  private static URI getUri(URI baseUri, String path) { 
    return UriBuilder.fromUri(baseUri).path(path).build();
  }

}
