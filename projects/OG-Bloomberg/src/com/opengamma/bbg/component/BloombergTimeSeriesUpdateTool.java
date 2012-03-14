/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.component.tool.AbstractTool;

/**
 * Updates time-series using the Bloomberg historical loader.
 */
public class BloombergTimeSeriesUpdateTool extends AbstractTool {

  @Override
  protected void doRun() throws Exception {
    if (!(getToolContext() instanceof BloombergToolContext)) {
      throw new OpenGammaRuntimeException("The " + BloombergTimeSeriesUpdateTool.class.getSimpleName() +
          " requires a tool context which implements " + BloombergToolContext.class.getName());
    }
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
        getToolContext().getHistoricalTimeSeriesMaster(),
        ((BloombergToolContext) getToolContext()).getBloombergHistoricalTimeSeriesSource(),
        new BloombergIdentifierProvider(((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider()));
    loader.setUpdateDb(true);
    loader.run();
  }
  
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new BloombergTimeSeriesUpdateTool().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

}
