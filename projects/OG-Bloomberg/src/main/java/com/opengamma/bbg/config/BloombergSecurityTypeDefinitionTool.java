/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to populate the config master with default bloomberg security type definitions
 */
@Scriptable
public class BloombergSecurityTypeDefinitionTool extends AbstractTool<ToolContext> {
  
  private static final String CONFIG_NAME_OPT = "n";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new BloombergSecurityTypeDefinitionTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    CommandLine commandLine = getCommandLine();
    
    BloombergSecurityTypeDefinitionLoader loader;
    if (commandLine.hasOption(CONFIG_NAME_OPT)) {
      loader = new BloombergSecurityTypeDefinitionLoader(configMaster, commandLine.getOptionValue(CONFIG_NAME_OPT));
    } else {
      loader = new BloombergSecurityTypeDefinitionLoader(configMaster);
    }
    loader.run();
  }
  
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("bbg-sec-type-defintion-tool.sh ...", options, true);
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createNameOption());
    return options;
  }
  
  @SuppressWarnings("static-access")
  private Option createNameOption() {
    return OptionBuilder.isRequired(false)
                        .hasArgs()
                        .withArgName("name config doc")
                        .withDescription("The name of the config document")
                        .withLongOpt("name")
                        .create(CONFIG_NAME_OPT);
  }

}
