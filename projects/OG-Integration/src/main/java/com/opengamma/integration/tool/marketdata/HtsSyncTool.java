/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.google.common.collect.Sets;
import com.opengamma.component.tool.AbstractDualComponentTool;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.scripts.Scriptable;

/**
 * The entry point for running OpenGamma batches. 
 */
@Scriptable
public class HtsSyncTool extends AbstractDualComponentTool {

  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new HtsSyncTool().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

  @Override
  protected void doRun() throws Exception {
    Map<String, HistoricalTimeSeriesMaster> srcHtsMasters = getSourceRemoteComponentFactory().getHistoricalTimeSeriesMasters();
    Map<String, HistoricalTimeSeriesMaster> destHtsMasters = getDestinationRemoteComponentFactory().getHistoricalTimeSeriesMasters();
    boolean fast = getCommandLine().hasOption("fast");
    boolean hardSync = getCommandLine().hasOption("hard-sync");
    boolean verbose = getCommandLine().hasOption("verbose");
    boolean noAdditions = getCommandLine().hasOption("no-additions");
    if (hardSync && noAdditions) {
      System.err.println("Cannot specify both hard-sync and no-additions options");
      return;
    }
    Set<String> filteredClassifiers = filterClassifiers(srcHtsMasters.keySet(), destHtsMasters.keySet());
    for (String classifier : filteredClassifiers) {
      HistoricalTimeSeriesMaster srcHtsMaster = srcHtsMasters.get(classifier);
      HistoricalTimeSeriesMaster destHtsMaster = destHtsMasters.get(classifier);
      HistoricalTimeSeriesMasterCopier copier = new HistoricalTimeSeriesMasterCopier(srcHtsMaster, destHtsMaster);
      copier.copy(fast, hardSync, verbose, noAdditions);
    }
  }
  
  private Set<String> filterClassifiers(Set<String> srcMasterClassifiers, Set<String> destMasterClassifiers) {
    Set<String> commonComponentNames = Sets.newLinkedHashSet();
    commonComponentNames.addAll(srcMasterClassifiers);
    commonComponentNames.retainAll(destMasterClassifiers);
    if (getCommandLine().hasOption("classifiers")) {
      List<String> classifiersList = Arrays.asList(getCommandLine().getOptionValues("classifiers"));
      Set<String> classifiers = Sets.newHashSet();
      classifiers.addAll(classifiersList);
      classifiers.removeAll(classifiers);
      if (classifiers.size() > 0) {
        System.err.println("Couldn't find classifiers: " + classifiers.toString() + ", skipping those");
      }
      classifiers.clear();
      classifiers.addAll(classifiersList);
      commonComponentNames.retainAll(classifiers);
    }    
    return commonComponentNames;
  }
  
  @SuppressWarnings("static-access")
  private Option createClassifiersOption() {
    return OptionBuilder.hasArgs()
                        .withArgName("classifier name")
                        .withDescription("specify classifiers for masters to sync")
                        .isRequired(false)
                        .withLongOpt("classifiers")
                        .create("c");
  }
  
  @SuppressWarnings("static-access")
  private Option createFastOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("assume only new data needs copying")
                        .isRequired(false)
                        .withLongOpt("fast")
                        .create("f");
  }
  
  @SuppressWarnings("static-access")
  private Option createHardSyncOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("remove time series at destination not present in source")
                        .isRequired(false)
                        .withLongOpt("hard-sync")
                        .create("h");
  }
  
  @SuppressWarnings("static-access")
  private Option createNoAdditionsOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("don't add any time series to the destination, only update what's there")
                        .isRequired(false)
                        .withLongOpt("no-additions")
                        .create("n");
  }
    
  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("show extra messages")
                        .isRequired(false)
                        .withLongOpt("verbose")
                        .create("v");
  }
  
  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption(createClassifiersOption());
    options.addOption(createVerboseOption());
    options.addOption(createFastOption());
    options.addOption(createHardSyncOption());
    options.addOption(createNoAdditionsOption());
    return options;
  }

}
