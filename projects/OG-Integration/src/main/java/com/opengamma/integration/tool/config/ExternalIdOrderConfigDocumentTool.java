/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master.
 * The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class ExternalIdOrderConfigDocumentTool extends AbstractTool<IntegrationToolContext> {

  private static final String DEFAULT_CONFIG_NAME = "DEFAULT";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExternalIdOrderConfigDocumentTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ConfigMaster master = getToolContext().getConfigMaster();
    ExternalIdOrderConfigDocumentLoader loader = new ExternalIdOrderConfigDocumentLoader(master, DEFAULT_CONFIG_NAME);
    loader.run();
  }

}
