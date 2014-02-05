/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master. The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class ConfigValidationTool extends AbstractTool<ToolContext> {
  
  private static final String ERRORS_PARTIAL_GRAPH_OPTION = "errors-partial-graph";
  private static final String ERRORS_FULL_GRAPH_OPTION = "errors-full-graph";
  private static final String VERBOSE_OPTION = "verbose";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { // CSIGNORE
    new ConfigValidationTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    ConfigSource configSource = toolContext.getConfigSource();
    ConventionSource conventionSource = toolContext.getConventionSource();
    RegionSource regionSource = toolContext.getRegionSource();
    SecuritySource securitySource = toolContext.getSecuritySource();
    HolidayMaster holidayMaster = toolContext.getHolidayMaster();
    CommandLine commandLine = getCommandLine();
    CurveValidator curveValidator = new CurveValidator(configMaster, configSource, conventionSource, regionSource, securitySource, holidayMaster);
    boolean verbose = commandLine.hasOption(VERBOSE_OPTION);
    if (verbose) {
      System.out.println("Starting validation...");
    }
    curveValidator.validateNewCurveSetup();
    if (verbose) {
      System.out.println("CurveConstructionConfiguration and linked objects");
      System.out.println("-------------------------------------------------");
    }
    List<ValidationNode> validateNewCurveSetup = curveValidator.getCurveConstructionConfigResults();
    for (ValidationNode node : validateNewCurveSetup) {
      if (verbose) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          System.out.println("Curve construction configuration " + node.getName() + " has errors and/or warnings");
        } else {
          System.out.println("Curve construction configuration " + node.getName() + " is good");
        }
      }
      if (commandLine.hasOption(ERRORS_PARTIAL_GRAPH_OPTION)) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          ValidationTreeUtils.propagateErrorsAndWarningsUp(node);
          ValidationTreeUtils.discardNonErrors(node);
          System.out.println(ValidationTextFormatter.formatTree(node));
        }
      } else if (commandLine.hasOption(ERRORS_FULL_GRAPH_OPTION)) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          System.out.println(ValidationTextFormatter.formatTree(node));
        }        
      } else {
        System.out.println(ValidationTextFormatter.formatTree(node));
      }     
    }
    if (verbose) {
      System.out.println("ExposureFunctions");
      System.out.println("-----------------");
    }
    List<ValidationNode> validateExposureConfigs = curveValidator.getExposureFunctionsConfigResults();
    for (ValidationNode node : validateExposureConfigs) {
      if (verbose) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          System.out.println("Exposure functions configuration " + node.getName() + " has errors and/or warnings");
        } else {
          System.out.println("Exposure functions configuration " + node.getName() + " is good");
        }
      }
      if (commandLine.hasOption(ERRORS_PARTIAL_GRAPH_OPTION)) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          ValidationTreeUtils.propagateErrorsAndWarningsUp(node);
          ValidationTreeUtils.discardNonErrors(node);
          System.out.println(ValidationTextFormatter.formatTree(node));
        }
      } else if (commandLine.hasOption(ERRORS_FULL_GRAPH_OPTION)) {
        if (ValidationTreeUtils.containsErrorsOrWarnings(node)) {
          System.out.println(ValidationTextFormatter.formatTree(node));
        }        
      } else {
        System.out.println(ValidationTextFormatter.formatTree(node));
      }     
    }    
    if (verbose) {
      System.out.println("Finished validation");
    }
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createErrorsGraphOnlyOption());
    options.addOption(createErrorsOnlyOption());
    options.addOption(createVerboseOption());
    return options;
  }

  @SuppressWarnings("static-access")
  protected Option createErrorsGraphOnlyOption() {
    return OptionBuilder.withDescription("Only print parts of config graph with errors").withLongOpt(ERRORS_PARTIAL_GRAPH_OPTION).create("p");
  }
  
  @SuppressWarnings("static-access")
  protected Option createErrorsOnlyOption() {
    return OptionBuilder.withDescription("Print full config graph only for configs containing errors").withLongOpt(ERRORS_FULL_GRAPH_OPTION).create("f");
  }
  
  @SuppressWarnings("static-access")
  protected Option createVerboseOption() {
    return OptionBuilder.withDescription("Print extra progress messages").withLongOpt(VERBOSE_OPTION).create("v");
  }

  @Override
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("config-validation-tool.sh [file...]", options, true);
  }

}
