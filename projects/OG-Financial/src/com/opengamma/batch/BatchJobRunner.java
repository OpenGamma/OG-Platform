/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.batch;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchJobRunner {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BatchJobRunner.class);

  /**
   * Date-time format: yyyyMMdd
   */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatters.pattern("yyyyMMdd");

  static LocalDate parseDate(String date) {
    return LocalDate.parse(date, DATE_FORMATTER);
  }

  static Instant parseTime(String date) {
    return OffsetDateTime.parse(date, DATE_FORMATTER).toInstant();
  }

  private static RunCreationMode getRunCreationMode(CommandLine line, Properties configProperties, String configPropertysFile) {
    String runCreationMode = getProperty("runCreationMode", line, configProperties, configPropertysFile, false);
    if (runCreationMode != null) {
      if (runCreationMode.equalsIgnoreCase("auto")) {
        return RunCreationMode.AUTO;
      } else if (runCreationMode.equalsIgnoreCase("create_new")) {
        return RunCreationMode.CREATE_NEW;
      } else if (runCreationMode.equalsIgnoreCase("create_new_overwrite")) {
        return RunCreationMode.CREATE_NEW_OVERWRITE;
      } else if (runCreationMode.equalsIgnoreCase("reuse_existing")) {
        return RunCreationMode.REUSE_EXISTING;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized runCreationMode. " +
          "Should be one of AUTO, ALWAYS, NEVER. " +
          "Was " + runCreationMode);
      }
    } else {
      return null;
    }
  }

  private static LocalDate getObservationDate(CommandLine line, Properties configProperties, String configPropertysFile) {
    String observationDate = getProperty("observationDate", line, configProperties, configPropertysFile, false);
    if (observationDate != null) {
      return parseDate(observationDate);
    } else {
      return LocalDate.now();
    }
  }


  private static Instant getValuationTime(CommandLine line, Properties configProperties, String configPropertysFile) {
    String observationDate = getProperty("valuationTime", line, configProperties, configPropertysFile, false);
    if (observationDate != null) {
      return parseTime(observationDate);
    } else {
      return Instant.now();
    }
  }

  private static UniqueId getViewDefinitionUniqueId(CommandLine line, Properties configProperties) {
    String view = getProperty("view", line, configProperties);
    if (view != null) {
      return UniqueId.parse(view);
    } else {
      throw new IllegalArgumentException("View definition unique Id is mandatory parameter");
    }
  }

  /**
   * Creates an runs a batch job based on a properties file and configuration.
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      usage();
      System.exit(-1);
    }
    
    CommandLine line = null;
    Properties configProperties = null;
    
    final String propertyFile = "batchJob.properties";

    String configPropertyFile = null;

    if (System.getProperty(propertyFile) != null) {
      configPropertyFile = System.getProperty(propertyFile);      
    try {
        FileInputStream fis = new FileInputStream(configPropertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
    } catch (FileNotFoundException e) {
      s_logger.error("The system cannot find " + configPropertyFile);            
      System.exit(-1);      
    }
    } else {
      try {
        FileInputStream fis = new FileInputStream(propertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
        configPropertyFile = propertyFile;
      } catch (FileNotFoundException e) {
        // there is no config file so we expect command line arguments
        try {
          CommandLineParser parser = new PosixParser();
          line = parser.parse(getOptions(), args);
        } catch (ParseException e2) {
          usage();
          System.exit(-1);
      }
    }
    }

    RunCreationMode runCreationMode = getRunCreationMode(line, configProperties, configPropertyFile);
    if (runCreationMode == null) {
      // default
      runCreationMode = RunCreationMode.AUTO;
    }

    String engineURI = getProperty("engineURI", line, configProperties, configPropertyFile);

    String brokerURL = getProperty("brokerURL", line, configProperties, configPropertyFile);

    Instant valuationTime = getValuationTime(line, configProperties, configPropertyFile);
    LocalDate observationDate = getObservationDate(line, configProperties, configPropertyFile);
    
    UniqueId viewDefinitionUniqueId = getViewDefinitionUniqueId(line, configProperties);

    URI vpBase;
    try {
      vpBase = new URI(engineURI);
    } catch (URISyntaxException ex) {
      throw new OpenGammaRuntimeException("Invalid URI", ex);
    }

    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
    activeMQConnectionFactory.setWatchTopicAdvisories(false);

    JmsConnectorFactoryBean jmsConnectorFactoryBean = new JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setConnectionFactory(activeMQConnectionFactory);
    jmsConnectorFactoryBean.setName("Masters");

    JmsConnector jmsConnector = jmsConnectorFactoryBean.getObjectCreating();
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      ViewProcessor vp = new RemoteViewProcessor(
        vpBase,
        jmsConnector,
        heartbeatScheduler);
      ViewClient vc = vp.createViewClient(UserPrincipal.getLocalUser());

      HistoricalMarketDataSpecification marketDataSpecification = MarketData.historical(observationDate, null, null);

      ViewExecutionOptions executionOptions = ExecutionOptions.batch(valuationTime, marketDataSpecification, null);

      vc.attachToViewProcess(viewDefinitionUniqueId, executionOptions);      
      vc.waitForCompletion();
    } finally {
      heartbeatScheduler.shutdown();
    }
    
    /*if (failed) {
      s_logger.error("Batch failed.");
      System.exit(-1);
    } else {
      s_logger.info("Batch succeeded.");
      System.exit(0);
    }*/
    }

  private static String getProperty(String property, CommandLine line, Properties configProperties) {
    return getProperty(property, line, configProperties, null);
  }

  private static String getProperty(String propertyName, CommandLine line, Properties properties, String configPropertysFile) {
    return getProperty(propertyName, line, properties, configPropertysFile, true);
  }

  private static String getProperty(String propertyName, CommandLine line, Properties properties, String configPropertysFile, boolean required) {
    String optionValue = null;
    if (line != null) {
      optionValue = line.getOptionValue(propertyName);
      if (optionValue != null) {
        return optionValue;
      }
    }
    if (properties != null) {
      optionValue = properties.getProperty(propertyName);
      if (optionValue == null && required) {
      s_logger.error("Cannot find property " + propertyName + " in " + configPropertysFile);            
      System.exit(-1);
    }
    } else {
      if (required) {
        s_logger.error("Cannot find option " + propertyName + " in command line arguments");
        System.exit(-1);
      }
    }
    return optionValue;
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java [-DbatchJob.properties={property file}] com.opengamma.financial.batch.BatchJobRunner [options]", getOptions());
  }

  private static Options getOptions() {
    Options options = new Options();

    //options.addOption("reason", true, "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");

    //options.addOption("observationTime", true, "Observation time - for example, LDN_CLOSE. Default - " + BatchJobParameters.AD_HOC_OBSERVATION_TIME + ".");
    options.addOption("observationDate", true, "Observation date (= run date). yyyyMMdd - for example, 20100621. Default - system clock date.");

    options.addOption("valuationTime", true, "Valuation time. HH:mm[:ss] - for example, 16:22:09. Default - system clock.");

    options.addOption("view", true, "View name in configuration database. You must specify this.");

    options.addOption("engineURI", true, "URI to remote OG engine - for example 'http://localhost:8080/jax/data/viewProcessors/Vp~0/'. You must specify this.");
    options.addOption("brokerURL", true, "URL to activeMQ broker - for example 'tcp://activemq.hq.opengamma.com:61616'. You must specify this.");

    //options.addOption("viewTime", true, "Time at which view should be loaded. HH:mm[:ss]. Default - system clock.");
    //    options.addOption("snapshotObservationTime", true, "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as observationTime.");
    //    options.addOption("snapshotObservationDate", true, "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationDate");

    options.addOption("runCreationMode", true, "One of auto, create_new, create_new_overwrite, reuse_existing (case insensitive)." +
      " Specifies whether to create a new run in the database." +
      " See documentation of RunCreationMode Java enum to find out more. Default - auto.");


    //options.addOption("timeZone", true, "Time zone in which times on the command line are given. Default - system time zone.");

    return options;
  }

}
