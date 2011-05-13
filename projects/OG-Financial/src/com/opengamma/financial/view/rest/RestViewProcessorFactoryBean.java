/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.concurrent.ScheduledExecutorService;

import javax.jms.ConnectionFactory;

import org.fudgemsg.FudgeContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring helper for {@link DataViewProcessorsResource}.
 */
public class RestViewProcessorFactoryBean extends SingletonFactoryBean<DataViewProcessorsResource> {

  private ViewProcessor _viewProcessor;
  private ConnectionFactory _connectionFactory;
  private FudgeContext _fudgeContext;
  private ScheduledExecutorService _scheduler;
   
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }

  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    _connectionFactory = connectionFactory;
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

  @Override
  protected DataViewProcessorsResource createObject() {
    if (_viewProcessor == null) {
      throw new OpenGammaRuntimeException("The viewProcessor property must be set");
    }
    if (_connectionFactory == null) {
      throw new OpenGammaRuntimeException("The connectionFactory property must be set");
    }
    if (_scheduler == null) {
      throw new OpenGammaRuntimeException("The scheduler property must be set");
    }
    
    DataViewProcessorsResource resource = new DataViewProcessorsResource();
    resource.addViewProcessor(getViewProcessor(), getConnectionFactory(), getFudgeContext(), getScheduler());
    return resource;
  }

}
