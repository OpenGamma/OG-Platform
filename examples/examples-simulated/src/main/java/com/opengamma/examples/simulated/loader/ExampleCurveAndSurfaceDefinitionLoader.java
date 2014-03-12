/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.curve.ExampleFXForwardCurveConfigPopulator;
import com.opengamma.examples.simulated.volatility.cube.ExampleSwaptionVolatilityCubeConfigPopulator;
import com.opengamma.examples.simulated.volatility.surface.ExampleATMSwaptionVolatilitySurfaceConfigPopulator;
import com.opengamma.examples.simulated.volatility.surface.ExampleFXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.examples.simulated.volatility.surface.ExampleForwardSwapSurfaceConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ExampleCurveAndSurfaceDefinitionLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new ExampleCurveAndSurfaceDefinitionLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    new YieldCurveConfigPopulator(configMaster, true);
    ExampleFXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(configMaster, ExampleViewsPopulator.CURRENCY_PAIRS);
    ExampleATMSwaptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(configMaster, ExampleViewsPopulator.SWAPTION_CURRENCY_CONFIGS);
    ExampleFXForwardCurveConfigPopulator.populateCurveConfigMaster(configMaster, ExampleViewsPopulator.CURRENCY_PAIRS);
    ExampleSwaptionVolatilityCubeConfigPopulator.populateVolatilityCubeConfigMaster(configMaster, ExampleViewsPopulator.SWAPTION_CURRENCY_CONFIGS);
    ExampleForwardSwapSurfaceConfigPopulator.populateSurfaceConfigMaster(configMaster, ExampleViewsPopulator.SWAPTION_COUNTRY_CONFIGS);
  }

}
