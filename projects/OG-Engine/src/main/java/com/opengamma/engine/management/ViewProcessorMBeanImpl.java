/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.HashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on ViewProcessor.
 * 
 */
public final class ViewProcessorMBeanImpl implements ViewProcessorMBean {

  /**
   * A ViewProcessor backing instance
   */
  private final ViewProcessorInternal _viewProcessor;
  
  private final ObjectName _objectName;
  
  /**
   * Create a management ViewProcessor
   *
   * @param viewProcessor the underlying ViewProcessor
   * @param splitByViewProcessor
   */
  public ViewProcessorMBeanImpl(ViewProcessorInternal viewProcessor, boolean splitByViewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    _viewProcessor = viewProcessor;
    _objectName = createObjectName(viewProcessor, splitByViewProcessor);
  }
  
  /**
   * Creates an object name using the scheme "com.opengamma:type=ViewProcessor,name=<viewProcessorName>"
   */
  static ObjectName createObjectName(ViewProcessor viewProcessor,
                                     boolean splitByViewProcessor) {
    try {
      return new ObjectName(splitByViewProcessor ?
        "com.opengamma:type=ViewProcessors,ViewProcessor=ViewProcessor " + viewProcessor.getName() + ",name=ViewProcessor " + viewProcessor.getName() :
        "com.opengamma:type=ViewProcessor,name=ViewProcessor " + viewProcessor.getName());
    } catch (MalformedObjectNameException e) {
      throw new OpenGammaRuntimeException("", e);
    }
  }

  @Override
  public Set<UniqueId> getViewProcesses() {
    Set<UniqueId> result = new HashSet<UniqueId>();
    for (com.opengamma.engine.view.ViewProcess viewProcess : _viewProcessor.getViewProcesses()) {
      result.add(viewProcess.getUniqueId());
    }
    return result;
  }

  
  @Override
  public int getNumberOfViewProcesses() {
    return _viewProcessor.getViewProcesses().size(); // == getViewProcesses().size();
  }

  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }

  @Override
  public void start() {
    _viewProcessor.start();
  }

  @Override
  public void stop() {
    _viewProcessor.stop();
  }

  @Override
  public boolean isRunning() {
    return _viewProcessor.isRunning();
  }
  
}
