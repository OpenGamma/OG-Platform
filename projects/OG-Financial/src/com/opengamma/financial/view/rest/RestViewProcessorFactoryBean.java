/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Spring helper for {@link DataViewProcessorsResource}.
 */
public class RestViewProcessorFactoryBean extends SingletonFactoryBean<DataViewProcessorsResource> {

  private final Collection<ViewProcessor> _viewProcessors = new HashSet<ViewProcessor>();
  private JmsConnector _jmsConnector;
  private FudgeContext _fudgeContext;
  private ScheduledExecutorService _scheduler;

  public Collection<ViewProcessor> getViewProcessors() {
    return _viewProcessors;
  }

  public void setViewProcessors(Collection<ViewProcessor> viewProcessors) {
    _viewProcessors.clear();
    _viewProcessors.addAll(viewProcessors);
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  public void setJmsConnector(JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }
  
  public void setExecutorService(ScheduledExecutorService scheduler) {
    _scheduler = scheduler;
  }

  //-------------------------------------------------------------------------
  @Override
  protected DataViewProcessorsResource createObject() {
    if (_viewProcessors.isEmpty()) {
      throw new OpenGammaRuntimeException("The viewProcessors must be set");
    }
    if (_jmsConnector == null) {
      throw new OpenGammaRuntimeException("The connectionFactory property must be set");
    }
    if (_scheduler == null) {
      throw new OpenGammaRuntimeException("The scheduler property must be set");
    }
    
    DataViewProcessorsResource resource = new DataViewProcessorsResource();
    for (ViewProcessor viewProcessor : getViewProcessors()) {
      resource.addViewProcessor(viewProcessor, getJmsConnector(), getFudgeContext(), getScheduler());
    }
    return resource;
  }

}
