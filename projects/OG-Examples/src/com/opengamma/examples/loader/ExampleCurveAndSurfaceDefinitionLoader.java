/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.examples.volatility.surface.ExampleEquityOptionSurfaceConfigPopulator;
import com.opengamma.examples.volatility.surface.ExampleFXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionSurfaceConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.SwaptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyPairsConfigPopulator;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * 
 */
@Scriptable
public class ExampleCurveAndSurfaceDefinitionLoader extends AbstractExampleTool {

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();

    new YieldCurveConfigPopulator(configMaster, false);
    new CurrencyMatrixConfigPopulator(configMaster);
    CurrencyPairsConfigPopulator.populateCurrencyPairsConfigMaster(configMaster);
    new SwaptionVolatilitySurfaceConfigPopulator(configMaster);
    new IRFutureOptionSurfaceConfigPopulator(configMaster);
    new ExampleFXOptionVolatilitySurfaceConfigPopulator(configMaster);
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
    new ExampleCurveAndSurfaceDefinitionLoader().initAndRun(args);
    System.exit(0);
  }

}
