/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import com.opengamma.component.tool.AbstractTool;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractExampleTool extends AbstractTool {

  /**
   * Example configuration for tools.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:toolcontext/toolcontext-example.properties";

  @Override
  public boolean initAndRun(String[] args) {
    return super.initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

}
