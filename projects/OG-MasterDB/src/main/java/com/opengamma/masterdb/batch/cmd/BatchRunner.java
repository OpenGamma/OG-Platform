/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.StartupUtils;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchRunner {

  /** The spring configuration. */
  public static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/masterdb/batch/cmd/batch-context.xml";
  
  static {
    StartupUtils.init();
  }

  /**
   * @see RunCreationMode
   */
  @SuppressWarnings("unused")
  private static RunCreationMode s_runCreationMode = RunCreationMode.AUTO;
  /**
   * @see SnapshotMode
   */
  @SuppressWarnings("unused")
  private static SnapshotMode s_snapshotMode = SnapshotMode.PREPARED;
  /**
   * Textual form of view definition unique id.
   */
  private static String s_viewDefinitionUid;
  /**
   * The valuation instant.
   */
  private static Instant s_valuationInstant = Instant.now();
  /**
   * The observation instant.
   */
  private static OffsetDateTime s_observationDateTime = OffsetDateTime.now();
  /**
   * Version correction as of.
   */
  private static Instant s_versionAsOf;
  /**
   * Version correction corrected to.
   */
  private static Instant s_correctedTo;

  public static void main(String[] args) throws Exception {  // CSIGNORE
    if (args.length == 0) {
      usage();
      System.exit(-1);
    }

    CommandLine line = null;
    try {
      CommandLineParser parser = new PosixParser();
      line = parser.parse(getOptions(), args);
      initialize(line);
    } catch (ParseException e) {
      usage();
      System.exit(-1);
    }

    AbstractApplicationContext appContext = null;


    try {
      appContext = getApplicationContext();
      appContext.start();

      ViewProcessor viewProcessor = appContext.getBean("viewProcessor", ViewProcessor.class);

      ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
      MarketDataSpecification marketDataSpec = new FixedHistoricalMarketDataSpecification(s_observationDateTime.toLocalDate());
      ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(s_valuationInstant).setMarketDataSpecification(marketDataSpec)
          .setResolverVersionCorrection(VersionCorrection.of(s_versionAsOf, s_correctedTo)).create();
      ViewCycleExecutionSequence executionSequence = ArbitraryViewCycleExecutionSequence.of(cycleOptions);

      ExecutionOptions executionOptions = new ExecutionOptions(executionSequence, ExecutionFlags.none().awaitMarketData().get(), null, null);

      viewClient.attachToViewProcess(UniqueId.parse(s_viewDefinitionUid), executionOptions);
    } finally {
      if (appContext != null) {
        appContext.close();
      }
    }

    /*if (failed) {
      s_logger.error("Batch failed.");
      System.exit(-1);
    } else {
      s_logger.info("Batch succeeded.");
      System.exit(0);
    }*/
  }

  private static void initialize(CommandLine line) throws OpenGammaRuntimeException {

    if (line.hasOption("runCreationMode")) {
      String creationMode = line.getOptionValue("runCreationMode");
      if (creationMode.equalsIgnoreCase("auto")) {
        s_runCreationMode = RunCreationMode.AUTO;
      } else if (creationMode.equalsIgnoreCase("create_new")) {
        s_runCreationMode = RunCreationMode.CREATE_NEW;
      } else if (creationMode.equalsIgnoreCase("create_new_overwrite")) {
        s_runCreationMode = RunCreationMode.CREATE_NEW_OVERWRITE;
      } else if (creationMode.equalsIgnoreCase("reuse_existing")) {
        s_runCreationMode = RunCreationMode.REUSE_EXISTING;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized runCreationMode. " +
          "Should be one of AUTO, CREATE_NEW, CREATE_NEW_OVERWRITE, REUSE_EXISTING. " +
          "Was " + creationMode);
      }
    }

    if (line.hasOption("snapshotMode")) {
      String snapshotMode = line.getOptionValue("snapshotMode");
      if (snapshotMode.equalsIgnoreCase("PREPARED")) {
        s_snapshotMode = SnapshotMode.PREPARED;
      } else if (snapshotMode.equalsIgnoreCase("WRITE_THROUGH")) {
        s_snapshotMode = SnapshotMode.WRITE_THROUGH;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized snapshotMode. " +
          "Should be one of PREPARED, WRITE_THROUGH. " +
          "Was " + snapshotMode);
      }
    }

    s_viewDefinitionUid = line.getOptionValue("viewDefinitionUid");

    if (line.hasOption("valuationInstant")) {
      s_valuationInstant = OffsetDateTime.parse(line.getOptionValue("valuationInstant")).toInstant();
    }

    if (line.hasOption("observationDateTime")) {
      s_observationDateTime = OffsetDateTime.parse(line.getOptionValue("observationDateTime"));
    }

    if (line.hasOption("versionAsOf")) {
      s_versionAsOf = OffsetDateTime.parse(line.getOptionValue("versionAsOf")).toInstant();
    }

    if (line.hasOption("correctedTo")) {
      s_correctedTo = OffsetDateTime.parse(line.getOptionValue("correctedTo")).toInstant();
    }
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.masterdb.batch.cmd.BatchRunner [options]", getOptions());
  }

  protected static AbstractApplicationContext getApplicationContext() {
    return new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
  }

  public static Options getOptions() {
    Options options = new Options();

    options.addOption("reason", true, "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");

    options.addOption("observationDateTime", true, "Observation instant (= market data snapshot instant). yyyy-MM-ddTHH:mm:ssZZ - for example, 2011-12-15T14:48:59.323Z. Default - system clock date.");

    options.addOption("valuationInstant", true, "Valuation instant. yyyy-MM-ddTHH:mm:ssZZ - for example, 2011-12-15T14:48:59.323Z. Default - system clock.");

    options.addOption("viewDefinitionUid", true, "View definition unique id in configuration database. You must specify this.");

    options.addOption("versionAsOf", true, "Version correction as of. yyyy-MM-ddTHH:mm:ssZZ - for example, 2011-12-15T14:48:59.323Z.");

    options.addOption("correctedTo", true, "Version corrected to. yyyy-MM-ddTHH:mm:ssZZ - for example, 2011-12-15T14:48:59.323Z.");

    options.addOption("runCreationMode", true, "One of AUTO, CREATE_NEW, CREATE_NEW_OVERWRITE, REUSE_EXISTING (case insensitive)." +
      " Specifies whether to create a new run in the database." +
      " See documentation of RunCreationMode Java enum to find out more. Default - auto.");

    options.addOption("snapshotMode", true, "One of PREPARED, WRITE_THROUGH (case insensitive)." +
      " Specifies whether to save market data in the batch database or such data should be present in advance of batch run.");

    return options;
  }

}
