/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 * Creates example exposure functions.
 */
public class ExampleExposureFunctionLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * @param args The standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExampleExposureFunctionLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExampleExposureFunctionConfigPopulator.populateConfigMaster(configMaster);
  }
}
