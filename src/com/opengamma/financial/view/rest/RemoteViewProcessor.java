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
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.rest.FudgeRestClient;

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
    return new RemoteView(DataViewProcessorResource.uriView(_baseUri, name), _jmsTemplate);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getViewNames() {
    URI uri = getUri(_baseUri, DataViewProcessorResource.PATH_VIEW_NAMES);
    return _client.access(uri).get(Set.class);
  }
  
  //-------------------------------------------------------------------------
  private static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).build(path);
  }

}
