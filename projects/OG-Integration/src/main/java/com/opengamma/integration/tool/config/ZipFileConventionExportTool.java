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
import com.opengamma.master.convention.ConventionMaster;

/**
 * 
 */
public class ZipFileConventionExportTool extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ZipFileConventionExportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    CommandLine commandLine = getCommandLine();
    ToolContext toolContext = getToolContext();
    ConventionMaster configMaster = toolContext.getConventionMaster();

    MultiFileConventionSaver saver = new MultiFileConventionSaver();
    saver.setZipFileName(commandLine.getOptionValue("f"));
    saver.setConventionMaster(configMaster);
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
                        .withDescription("Zip file to dump to")
                        .withLongOpt("file")
                        .create("f");
  }
  
}
