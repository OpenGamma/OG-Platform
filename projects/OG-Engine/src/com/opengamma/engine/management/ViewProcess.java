/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.opengamma.engine.view.ViewProcessInternal;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a View.
 * 
 */
public class ViewProcess implements ViewProcessMBean {

  /**
   * A View backing instance
   */
  private final ViewProcessInternal _viewProcess;

  private final ObjectName _objectName;

  /**
   * Create a management View
   * 
   * @param viewProcess the underlying View
   * @param viewProcessor the viewProcessor processing the view
   */
  public ViewProcess(ViewProcessInternal viewProcess, com.opengamma.engine.view.ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _viewProcess = viewProcess;
    _objectName = createObjectName(viewProcessor.getUniqueId(), viewProcess.getUniqueId());
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewName>"
   */
  static ObjectName createObjectName(UniqueIdentifier viewProcessorId, UniqueIdentifier viewProcessId) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=ViewProcess,ViewProcessor=ViewProcessor " + viewProcessorId.getValue() + ",name=ViewProcess " + viewProcessId.getValue());
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }
  
  @Override
  public UniqueIdentifier getUniqueId() {
    return _viewProcess.getUniqueId();
  }
  
  @Override
  public String getPortfolioIdentifier() {
    return _viewProcess.getDefinition().getPortfolioId().toString();
  }

  @Override
  public String getDefinitionName() {
    return _viewProcess.getDefinitionName();
  }

  @Override
  public ViewProcessState getState() {
    return _viewProcess.getState();
  }

  @Override
  public void shutdown() {
    _viewProcess.shutdown();
  }
  
  @Override
  public void suspend() {
    _viewProcess.suspend();
  }

  @Override
  public void resume() {
    _viewProcess.resume();
  }
  
  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }
  
}
