/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.config;

import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Tool to read currency pairs from a text file and store them in the config master.
 * The pairs must be in the format AAA/BBB, one per line in the file.
 */
public class CurrencyPairsConfigDocumentTool extends AbstractProductionTool {

  /**
   * Main method to run the tool.
   * 
   * @param args 1) path to the currency pair data file
   *             2) name under which the {@link CurrencyPairs} should be saved in the config master
   */
  public static void main(String[] args) {  // CSIGNORE
    if (args.length < 2) {
      throw new IllegalArgumentException("2 args required: dataFile, configName");
    }
    new CurrencyPairsConfigDocumentTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ConfigMaster master = getToolContext().getConfigMaster();
    String[] args = getCommandLine().getArgs();
    CurrencyPairsConfigDocumentLoader loader = new CurrencyPairsConfigDocumentLoader(master, args[0], args[1]);
    loader.run();
  }

}
