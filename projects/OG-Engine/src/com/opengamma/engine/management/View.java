/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a View.
 * 
 */
public class View implements ViewMBean {

  /**
   * A View backing instance
   */
  private final com.opengamma.engine.view.View _view;

  private final ObjectName _objectName;

  /**
   * Create a management View
   * 
   * @param view the underlying View
   * @param viewProcessor the viewProcessor processing the view
   */
  public View(com.opengamma.engine.view.View view, com.opengamma.engine.view.ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(view, "View");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _view = view;
    _objectName = createObjectName(viewProcessor.toString(), view.getName());
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewName>"
   */
  static ObjectName createObjectName(String viewProcessorName, String viewName) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=View,ViewProcessor=" + viewProcessorName + ",name=" + viewName);
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }

  @Override
  public String getName() {
    return _view.getName();
  }

  @Override
  public void init() {
    _view.init();
  }

  @Override
  public String getPortfolio() {
    return _view.getPortfolio().getName();
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return _view.getAllSecurityTypes();
  }

  @Override
  public Set<String> getRequiredLiveData() {
    Set<String> result = Sets.newHashSet();
    for (ValueRequirement valueRequirement : _view.getRequiredLiveData()) {
      result.add(valueRequirement.toString());
    }
    return result;
  }

  @Override
  public void runOneCycle() {
    _view.runOneCycle();
  }

  @Override
  public void runOneCycle(long valuationTime) {
    _view.runOneCycle(valuationTime);
  }

  @Override
  public boolean isLiveComputationRunning() {
    return _view.isLiveComputationRunning();
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
