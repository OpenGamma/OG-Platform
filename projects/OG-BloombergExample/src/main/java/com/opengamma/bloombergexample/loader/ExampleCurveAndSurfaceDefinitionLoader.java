/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.SwaptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ExampleCurveAndSurfaceDefinitionLoader extends AbstractTool<ToolContext> {

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    new YieldCurveConfigPopulator(configMaster, false);
    new SwaptionVolatilitySurfaceConfigPopulator(configMaster);
    new IRFutureOptionSurfaceConfigPopulator(configMaster);
    //new ExampleFXOptionVolatilitySurfaceConfigPopulator(configMaster); // TODO: FX Option Volatility Surface
    new EquityOptionSurfaceConfigPopulator(configMaster);
    new VolatilityCubeConfigPopulator(configMaster);
    new FXForwardCurveConfigPopulator(configMaster);
    //new ExampleEquityOptionSurfaceConfigPopulator(configMaster); // TODO: Equity Option Surface
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleCurveAndSurfaceDefinitionLoader().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

}
