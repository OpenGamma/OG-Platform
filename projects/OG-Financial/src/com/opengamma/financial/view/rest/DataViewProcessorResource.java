/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;

/**
 * RESTful resource for a {@link ViewProcessor}.
 */
public class DataViewProcessorResource {

  //CSOFF: just constants
  public static final String PATH_VIEW_NAMES = "viewNames";
  public static final String PATH_REINIT_ASYNC = "reinitAsync";
  //CSON: just constants
  
  private final ViewProcessor _viewProcessor;
  private final String _jmsTopicPrefix;
  private final JmsByteArrayMessageSenderService _jmsMessageSenderService;
  
  public DataViewProcessorResource(ViewProcessor viewProcessor, ActiveMQConnectionFactory connectionFactory, String jmsTopicPrefix) {
    _viewProcessor = viewProcessor;
    
    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    jmsTemplate.setPubSubDomain(true);
    
    _jmsMessageSenderService = new JmsByteArrayMessageSenderService(jmsTemplate);
    _jmsTopicPrefix = jmsTopicPrefix;
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_VIEW_NAMES)
  public Response getViewNames() {
    return Response.ok(_viewProcessor.getViewNames()).build();
  }
  
  @Path("views/{viewName}")
  public DataViewResource getView(@PathParam("viewName") String viewName) {
    // TODO: authentication of the remote user while still providing RESTful access.
    UserPrincipal user = UserPrincipal.getLocalUser();
    
    View view = _viewProcessor.getView(viewName, user);
    if (view == null) {
      return null;
    }
    return new DataViewResource(view, _jmsMessageSenderService, _jmsTopicPrefix);
  }
  
  @POST
  @Path(PATH_REINIT_ASYNC)
  public Response reinitAsync() {
    _viewProcessor.reinitAsync();
    return Response.ok().build();
  }
  
  //-------------------------------------------------------------------------
  public static URI uriView(URI baseUri, String viewName) {
    // WARNING: '/' characters could well appear in the view name
    // There is a bug(?) in UriBuilder where, even though segment() is meant to treat the item as a single path segment
    // and therefore encode '/' characters, it does not encode '/' characters which come from a variable substitution.
    return UriBuilder.fromUri(baseUri).path("views").segment(viewName).build();
  }
  
}
