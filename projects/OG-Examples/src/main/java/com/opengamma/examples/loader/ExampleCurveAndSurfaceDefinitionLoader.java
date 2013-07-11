/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.curve.ExampleFXForwardCurveConfigPopulator;
import com.opengamma.examples.volatility.surface.ExampleATMSwaptionVolatilitySurfaceConfigPopulator;
import com.opengamma.examples.volatility.surface.ExampleEquityOptionSurfaceConfigPopulator;
import com.opengamma.examples.volatility.surface.ExampleFXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.SwaptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ExampleCurveAndSurfaceDefinitionLoader extends AbstractTool<ToolContext> {

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    new YieldCurveConfigPopulator(configMaster, true);
    new SwaptionVolatilitySurfaceConfigPopulator(configMaster);
    new IRFutureOptionSurfaceConfigPopulator(configMaster);
    ExampleFXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(configMaster, ExampleViewsPopulator.CURRENCY_PAIRS);
    ExampleATMSwaptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(configMaster, ExampleViewsPopulator.SWAPTION_SURFACES);
    ExampleFXForwardCurveConfigPopulator.populateCurveConfigMaster(configMaster, ExampleViewsPopulator.CURRENCY_PAIRS);
    new EquityOptionSurfaceConfigPopulator(configMaster);
    new VolatilityCubeConfigPopulator(configMaster);
    new FXForwardCurveConfigPopulator(configMaster);
    new ExampleEquityOptionSurfaceConfigPopulator(configMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the arguments, unused
   */
  public static void main(final String[] args) {  // CSIGNORE
    new ExampleCurveAndSurfaceDefinitionLoader().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

}
