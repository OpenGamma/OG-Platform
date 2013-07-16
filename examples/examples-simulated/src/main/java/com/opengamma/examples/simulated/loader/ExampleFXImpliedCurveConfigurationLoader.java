/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 * 
 */
public class ExampleFXImpliedCurveConfigurationLoader extends AbstractTool<ToolContext> {

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    new ExampleFXImpliedMultiCurveCalculationConfigPopulator(configMaster);
  }

  /**
   * Main method to run the tool
   * 
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { //CSIGNORE
    new ExampleFXImpliedCurveConfigurationLoader().initAndRun(args, ToolContext.class);
    System.exit(0);
  }
}
