/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool;

import com.opengamma.component.tool.AbstractTool;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractProductionTool extends AbstractTool {

  /**
   * Gets the tool context.
   * 
   * @return the context, not null during {@code doRun}
   */
  protected ProductionToolContext getToolContext() {
    return (ProductionToolContext) super.getToolContext();
  }

}
