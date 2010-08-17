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

import com.opengamma.engine.fudgemsg.EngineFudgeContextConfiguration;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.LocalViewProcessorClient;
import com.opengamma.engine.view.client.ViewProcessorClient;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.UtilFudgeContextConfiguration;

/**
 * RESTful resource publishing details of view processors.
 * <p>
 * Each view processor manages a collection of views.
 * Both views and view processors are uniquely referenced by name.
 */
@Path("viewProcessor")
public class ViewProcessorService {

  /**
   * Map of views keyed by view processor name.
   */
  private final ConcurrentMap<String, ViewProcessorResource> _viewProcessorMap = new ConcurrentHashMap<String, ViewProcessorResource>();
  /**
   * The spring JMS template.
   */
  private final JmsTemplate _jmsTemplate = new JmsTemplate();
  /**
   * The JMS topic prefix.
   */
  private String _topicPrefix;
  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;

  /**
   * User to run as 
   */
  private UserPrincipal _user;

  /**
   * Creates an instance with default values.
   */
  public ViewProcessorService() {
    setTopicPrefix("ViewProcessor");
    final FudgeContext fudgeContext = new FudgeContext();
    UtilFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    setFudgeContext(fudgeContext);
    getJmsTemplate().setPubSubDomain(true);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS topic prefix.
   * @return the topic prefix.
   */
  public String getTopicPrefix() {
    return _topicPrefix;
  }

  /**
   * Sets the topic prefix.
   * @param topicPrefix  the topic prefix
   */
  public void setTopicPrefix(final String topicPrefix) {
    _topicPrefix = topicPrefix;
  }

  /**
   * Gets the Fudge context.
   * @return the Fudge context
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _fudgeContext = fudgeContext;
  }

  public void setUser(final UserPrincipal user) {
    _user = user;
  }

  /**
   * Gets the JMS template.
   * @return the JMS template, not null
   */
  protected JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Sets the JMS connection factory.
   * This alters the JMS template.
   * @param connectionFactory  the factory
   */
  public void setConnectionFactory(final ConnectionFactory connectionFactory) {
    getJmsTemplate().setConnectionFactory(connectionFactory);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a view processor by name, exposed over REST.
   * @param name  the view processor name from the URI, not null
   * @return the resource representing the view processor, null if not found
   */
  @Path("{processorName}")
  public ViewProcessorResource findViewProcessor(@PathParam("processorName") String name) {
    return getViewProcessorMap().get(name);
  }

  /**
   * Gets the map of view processors.
   * @return the map, not null
   */
  protected ConcurrentMap<String, ViewProcessorResource> getViewProcessorMap() {
    return _viewProcessorMap;
  }

  /**
   * Adds a view processor to those published.
   * @param name  the view name, not null
   * @param viewProcessorResource  the view processor resource, not null
   */
  protected void addViewProcessor(final String name, final ViewProcessorResource viewProcessorResource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(viewProcessorResource, "view processor resource");
    getViewProcessorMap().put(name, viewProcessorResource);
  }

  /**
   * Adds a view processor to those published.
   * @param name  the view name, not null
   * @param viewProcessorClient  the view processor, not null
   */
  protected void addViewProcessor(final String name, final ViewProcessorClient viewProcessorClient) {
    ViewProcessorResource res = new ViewProcessorResource(getJmsTemplate(), getTopicPrefix() + "-" + name,
        getFudgeContext(), viewProcessorClient);
    addViewProcessor(name, res);
  }

  /**
   * Adds a view processor to those published.
   * @param name  the view name, not null
   * @param viewProcessor  the view processor, not null
   */
  protected void addViewProcessor(final String name, final ViewProcessor viewProcessor) {
    addViewProcessor(name, new LocalViewProcessorClient(viewProcessor, _user));
  }

  /**
   * Sets the default view processor.
   * @param viewProcessor  the view processor, not null
   */
  public void setViewProcessor(final ViewProcessor viewProcessor) {
    addViewProcessor(DEFAULT_VIEWPROCESSOR_NAME, viewProcessor);
  }

  /**
   * Sets the entire map of view processors.
   * @param viewProcessors  the map of view processors keyed by name, not null
   */
  public void setViewProcessorMap(Map<String, ViewProcessor> viewProcessors) {
    final ConcurrentMap<String, ViewProcessorResource> map = getViewProcessorMap();
    map.clear();
    for (Map.Entry<String, ViewProcessor> entry : viewProcessors.entrySet()) {
      addViewProcessor(entry.getKey(), entry.getValue());
    }
  }

}
