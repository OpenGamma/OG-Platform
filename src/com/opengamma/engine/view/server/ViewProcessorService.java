/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view.server;

import static com.opengamma.engine.view.server.ViewProcessorServiceNames.DEFAULT_VIEWPROCESSOR_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.ConnectionFactory;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.LocalViewProcessorClient;
import com.opengamma.engine.view.client.RemoteViewProcessorClient;
import com.opengamma.engine.view.client.ViewProcessorClient;

/**
 * RESTful service backend for {@link RemoteViewProcessorClient}.
 * 
 * @author Andrew Griffin
 */
@Path ("viewProcessor")
public class ViewProcessorService {
  
  private final ConcurrentMap<String,ViewProcessorResource> _viewProcessorMap = new ConcurrentHashMap<String,ViewProcessorResource> ();
  private final JmsTemplate _jmsTemplate = new JmsTemplate ();
  
  private String _topicPrefix;
  private FudgeContext _fudgeContext;
  
  public ViewProcessorService () {
    setTopicPrefix ("ViewProcessor");
    final FudgeContext fudgeContext = new FudgeContext ();
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext (fudgeContext);
    setFudgeContext (fudgeContext);
    getJmsTemplate ().setPubSubDomain (true);
  }
  
  public void setTopicPrefix (final String topicPrefix) {
    _topicPrefix = topicPrefix;
  }
  
  public String getTopicPrefix () {
    return _topicPrefix;
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  public void setFudgeContext (final FudgeContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext cannot be null");
    _fudgeContext = fudgeContext;
  }
  
  protected JmsTemplate getJmsTemplate () {
    return _jmsTemplate;
  }
  
  public void setConnectionFactory (final ConnectionFactory connectionFactory) {
    getJmsTemplate ().setConnectionFactory (connectionFactory);
  }
  
  protected ConcurrentMap<String,ViewProcessorResource> getViewProcessorMap () {
    return _viewProcessorMap;
  }
  
  @Path ("{processorName}")
  public ViewProcessorResource findViewProcessor (@PathParam ("processorName") String processorName) {
    return getViewProcessorMap ().get (processorName);
  }
  
  protected void addViewProcessor (final String name, final ViewProcessorResource viewProcessorResource) {
    getViewProcessorMap ().put (name, viewProcessorResource);
  }
  
  protected void addViewProcessor (final String name, final ViewProcessorClient viewProcessorClient) {
    addViewProcessor (name, new ViewProcessorResource (getJmsTemplate (), getTopicPrefix () + "-" + name, getFudgeContext (), viewProcessorClient));
  }
  
  protected void addViewProcessor (final String name, final ViewProcessor viewProcessor) {
    addViewProcessor (name, new LocalViewProcessorClient (viewProcessor));
  }
  
  public void setViewProcessor (final ViewProcessor viewProcessor) {
    addViewProcessor (DEFAULT_VIEWPROCESSOR_NAME, viewProcessor);
  }
  
  public void setViewProcessorMap (Map<String,ViewProcessor> viewProcessors) {
    final ConcurrentMap<String,ViewProcessorResource> map = getViewProcessorMap ();
    map.clear ();
    for (Map.Entry<String,ViewProcessor> entry : viewProcessors.entrySet ()) {
      addViewProcessor (entry.getKey (), entry.getValue ());
    }
  }

}