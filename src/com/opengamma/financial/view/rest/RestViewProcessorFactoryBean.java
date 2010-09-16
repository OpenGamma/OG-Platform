/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring helper for {@link DataViewProcessorsResource}.
 */
public class RestViewProcessorFactoryBean extends SingletonFactoryBean<DataViewProcessorsResource> {

  private ViewProcessor _viewProcessor;
  private ActiveMQConnectionFactory _connectionFactory;
  private String _topicPrefix;
   
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }

  public ActiveMQConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
    _connectionFactory = connectionFactory;
  }

  public String getTopicPrefix() {
    return _topicPrefix;
  }

  public void setTopicPrefix(String topicPrefix) {
    _topicPrefix = topicPrefix;
  }

  @Override
  protected DataViewProcessorsResource createObject() {
    if (_viewProcessor == null) {
      throw new OpenGammaRuntimeException("The viewProcessor property must be set");
    }
    if (_connectionFactory == null) {
      throw new OpenGammaRuntimeException("The connectionFactory property must be set");
    }
    if (_topicPrefix == null) {
      throw new OpenGammaRuntimeException("The topicPrefix property must be set");
    }
    
    DataViewProcessorsResource resource = new DataViewProcessorsResource();
    resource.addViewProcessor(DataViewProcessorsResource.DEFAULT_VIEW_PROCESSOR_NAME, getViewProcessor(), getConnectionFactory(), getTopicPrefix());
    return resource;
  }

}
