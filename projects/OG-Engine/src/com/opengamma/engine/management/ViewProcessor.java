/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.HashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessorInternal;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on ViewProcessor.
 * 
 */
public final class ViewProcessor implements ViewProcessorMBean {

  /**
   * A ViewProcessor backing instance
   */
  private final ViewProcessorInternal _viewProcessor;
  
  private final ObjectName _objectName;
  
  /**
   * Create a management ViewProcessor
   * 
   * @param viewProcessor the underlying ViewProcessor
   */
  public ViewProcessor(ViewProcessorInternal viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    _viewProcessor = viewProcessor;
    _objectName = createObjectName(viewProcessor);
  }
  
  /**
   * Creates an object name using the scheme "com.opengamma:type=ViewProcessor,name=<viewProcessorName>"
   */
  static ObjectName createObjectName(com.opengamma.engine.view.ViewProcessor viewProcessor) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=ViewProcessor,name=" + viewProcessor.toString());
    } catch (MalformedObjectNameException e) {
      throw new OpenGammaRuntimeException("", e);
    }
    return objectName;
  }

  @Override
  public Set<UniqueIdentifier> getViewProcesses() {
    Set<UniqueIdentifier> result = new HashSet<UniqueIdentifier>();
    for (com.opengamma.engine.view.ViewProcess viewProcess : _viewProcessor.getViewProcesses()) {
      result.add(viewProcess.getUniqueId());
    }
    return result;
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
