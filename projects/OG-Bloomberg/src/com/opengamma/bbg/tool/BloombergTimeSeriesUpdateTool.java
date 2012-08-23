/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.tool;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Updates time-series using the Bloomberg historical loader.
 */
@Scriptable
public class BloombergTimeSeriesUpdateTool extends AbstractBloombergTool {

  @Override
  protected void doRun() throws Exception {
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
        getToolContext().getHistoricalTimeSeriesMaster(),
        getBloombergToolContext().getBloombergHistoricalTimeSeriesSource(),
        new BloombergIdentifierProvider(getBloombergToolContext().getBloombergReferenceDataProvider()));
    loader.setUpdateDb(true);
    loader.setReload(getCommandLine().hasOption("reload"));
    loader.run();
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(new Option("r", "reload", false, "Reload complete time series"));
    return options;
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
