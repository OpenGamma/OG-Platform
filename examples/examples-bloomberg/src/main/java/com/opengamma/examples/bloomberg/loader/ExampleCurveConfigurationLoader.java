/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 *
 */
public class ExampleCurveConfigurationLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { //CSIGNORE
    new ExampleCurveConfigurationLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    new MultiCurveCalculationConfigPopulator(configMaster);
  }

}
