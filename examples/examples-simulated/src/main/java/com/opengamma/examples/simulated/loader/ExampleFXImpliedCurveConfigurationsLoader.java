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
 * Loads example FX implied curve configurations, definitions and curve node id mappers.
 */
public class ExampleFXImpliedCurveConfigurationsLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * @param args The standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExampleFXImpliedCurveConfigurationsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExampleFXImpliedCurveConfigurationsPopulator.populateConfigAndConventionMaster(configMaster);
  }
}
