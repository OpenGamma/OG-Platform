/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.tool;

import org.apache.commons.lang.ClassUtils;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractBloombergTool extends AbstractTool {

  /**
   * Gets the Bloomberg tool context.
   * 
   * @return the context, not null during {@code doRun}
   */
  protected BloombergToolContext getBloombergToolContext() {
    ToolContext toolContext = super.getToolContext();
    if (toolContext instanceof BloombergToolContext == false) {
      throw new ClassCastException("Tool context must implement BloombergToolContext, but was: " + ClassUtils.getShortClassName(toolContext, "null"));
    }
    return (BloombergToolContext) toolContext;
  }

}
