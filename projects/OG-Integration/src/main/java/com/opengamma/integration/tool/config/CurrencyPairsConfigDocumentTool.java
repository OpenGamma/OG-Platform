/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master.
 * The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class CurrencyPairsConfigDocumentTool extends AbstractTool<IntegrationToolContext> {

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
    new CurrencyPairsConfigDocumentTool().invokeAndTerminate(args);
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
