/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool;

import com.opengamma.component.tool.AbstractTool;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractIntegrationTool extends AbstractTool {

  /**
   * Gets the tool context.
   * 
   * @return the context, not null during {@code doRun}
   */
  protected IntegrationToolContext getToolContext() {
    return (IntegrationToolContext) super.getToolContext();
  }

}
