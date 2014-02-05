/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 * 
 */
public class MultiFileConfigExportTool extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new MultiFileConfigExportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    CommandLine commandLine = getCommandLine();
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();

    MultiFileConfigSaver saver = new MultiFileConfigSaver();
    saver.setDestinationDirectory(commandLine.getOptionValue("d"));
    saver.setConfigMaster(configMaster);
    saver.run();
  }

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createDirectoryOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createDirectoryOption() {
    return OptionBuilder.isRequired(true)
                        .hasArg(true)
                        .withDescription("Directory to save files to")
                        .withLongOpt("directory")
                        .create("d");
  }
  
}
