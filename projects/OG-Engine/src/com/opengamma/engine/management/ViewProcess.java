/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
  private final ViewProcessInternal _view;

  private final ObjectName _objectName;

  /**
   * Create a management View
   * 
   * @param view the underlying View
   * @param viewProcessor the viewProcessor processing the view
   */
  public ViewProcess(ViewProcessInternal view, com.opengamma.engine.view.ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(view, "View");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _view = view;
    _objectName = createObjectName(viewProcessor.toString(), view.getUniqueId());
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewName>"
   */
  static ObjectName createObjectName(String viewProcessorName, UniqueIdentifier viewProcessId) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=View,ViewProcessor=" + viewProcessorName + ",name=" + viewProcessId);
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }
  
  @Override
  public UniqueIdentifier getUniqueId() {
    return _view.getUniqueId();
  }
  
  @Override
  public String getPortfolioIdentifier() {
    return _view.getDefinition().getPortfolioId().toString();
  }

  @Override
  public boolean isBatchProcess() {
    return _view.isBatchProcess();
  }

  @Override
  public String getDefinitionName() {
    return _view.getDefinitionName();
  }

  @Override
  public ViewProcessState getState() {
    return _view.getState();
  }

  @Override
  public void shutdown() {
    _view.shutdown();
  }
  
  @Override
  public void suspend() {
    _view.suspend();
  }

  @Override
  public void resume() {
    _view.resume();
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
