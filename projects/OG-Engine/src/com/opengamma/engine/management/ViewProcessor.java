/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on ViewProcessor.
 * 
 */
public final class ViewProcessor implements ViewProcessorMBean {

  /**
   * A ViewProcessor backing instance
   */
  private final com.opengamma.engine.view.ViewProcessor _viewProcessor;
  
  private final ObjectName _objectName;
  
  /**
   * Create a management ViewProcessor
   * 
   * @param viewProcessor the underlying ViewProcessor
   */
  public ViewProcessor(com.opengamma.engine.view.ViewProcessor viewProcessor) {
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
  public Set<String> getViewNames() {
    return _viewProcessor.getViewNames();
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
