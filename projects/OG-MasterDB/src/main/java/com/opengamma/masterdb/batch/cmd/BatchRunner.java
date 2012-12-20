/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.cmd;

import javax.time.Instant;
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.OffsetDateTime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchRunner {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BatchRunner.class);

  /** The spring configuration. */
  public static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/masterdb/batch/cmd/batch-context.xml";

  /**
   * @see RunCreationMode
   */
  private static RunCreationMode _runCreationMode = RunCreationMode.AUTO;

  /**
   * @see SnapshotMode
   */
  private static SnapshotMode _snapshotMode = SnapshotMode.PREPARED;

  /**
   * Textual form of view definition unique id.
   */
  private static String _viewDefinitionUid;

  /**
   * The valuation instant.
   */
  private static Instant _valuationInstant = Instant.now();

  /**
   * The observation instant.
   */
  private static DateTimeProvider _observationDateTime = OffsetDateTime.now();

  /**
   * Version correction as of.
   */
  private static Instant _versionAsOf;
  /**
   * Version correction corrected to.
   */
  private static Instant _correctedTo;

  public static void main(String[] args) throws Exception {
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
      MarketDataSpecification marketDataSpec = new FixedHistoricalMarketDataSpecification(_observationDateTime);
      ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(_valuationInstant, marketDataSpec);
      ViewCycleExecutionSequence executionSequence = ArbitraryViewCycleExecutionSequence.of(cycleOptions);

      VersionCorrection versionCorrection = VersionCorrection.of(_versionAsOf, _correctedTo);

      ExecutionOptions executionOptions = new ExecutionOptions(executionSequence, ExecutionFlags.none().awaitMarketData().get(), null, null, versionCorrection);

      viewClient.attachToViewProcess(UniqueId.parse(_viewDefinitionUid), executionOptions);
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
        _runCreationMode = RunCreationMode.AUTO;
      } else if (creationMode.equalsIgnoreCase("create_new")) {
        _runCreationMode = RunCreationMode.CREATE_NEW;
      } else if (creationMode.equalsIgnoreCase("create_new_overwrite")) {
        _runCreationMode = RunCreationMode.CREATE_NEW_OVERWRITE;
      } else if (creationMode.equalsIgnoreCase("reuse_existing")) {
        _runCreationMode = RunCreationMode.REUSE_EXISTING;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized runCreationMode. " +
          "Should be one of AUTO, CREATE_NEW, CREATE_NEW_OVERWRITE, REUSE_EXISTING. " +
          "Was " + creationMode);
      }
    }

    if (line.hasOption("snapshotMode")) {
      String snapshotMode = line.getOptionValue("snapshotMode");
      if (snapshotMode.equalsIgnoreCase("PREPARED")) {
        _snapshotMode = SnapshotMode.PREPARED;
      } else if (snapshotMode.equalsIgnoreCase("WRITE_THROUGH")) {
        _snapshotMode = SnapshotMode.WRITE_THROUGH;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized snapshotMode. " +
          "Should be one of PREPARED, WRITE_THROUGH. " +
          "Was " + snapshotMode);
      }
    }

    _viewDefinitionUid = line.getOptionValue("viewDefinitionUid");

    if (line.hasOption("valuationInstant")) {
      _valuationInstant = OffsetDateTime.parse(line.getOptionValue("valuationInstant")).toInstant();
    }

    if (line.hasOption("observationDateTime")) {
      _observationDateTime = OffsetDateTime.parse(line.getOptionValue("observationDateTime"));
    }

    if (line.hasOption("versionAsOf")) {
      _versionAsOf = OffsetDateTime.parse(line.getOptionValue("versionAsOf")).toInstant();
    }

    if (line.hasOption("correctedTo")) {
      _correctedTo = OffsetDateTime.parse(line.getOptionValue("correctedTo")).toInstant();
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
