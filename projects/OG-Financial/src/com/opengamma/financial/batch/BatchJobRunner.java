/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbConnectorFactoryBean;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchJobRunner {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BatchJobRunner.class);

  /**
   * Creates an runs a batch job based on a properties file and configuration.
   */
  public static void main(String[] args) throws Exception { // CSIGNORE
    if (args.length == 0) {
      usage();
      System.exit(-1);
    }
    
    CommandLine line = null;
    try {
      CommandLineParser parser = new PosixParser();
      line = parser.parse(CommandLineBatchJob.getOptions(), args);
    } catch (ParseException e) {
      usage();
      System.exit(-1);
    }
    
    String configName = args[args.length - 1];
    
    final String propertyFile = "batchJob.properties";
    String configPropertyFile = propertyFile;
    if (System.getProperty(propertyFile) != null) {
      configPropertyFile = System.getProperty(propertyFile);      
    }
    
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configPropertyFile);
    } catch (FileNotFoundException e) {
      s_logger.error("The system cannot find " + configPropertyFile);            
      System.exit(-1);      
    }
    Properties configDbProperties = new Properties();
    configDbProperties.load(fis);
    fis.close();
    
    String managerClass = getProperty(configDbProperties, "opengamma.config.db.configmaster", configPropertyFile);
    String driver = getProperty(configDbProperties, "opengamma.config.jdbc.driver", configPropertyFile);
    String url = getProperty(configDbProperties, "opengamma.config.jdbc.url", configPropertyFile);
    String username = getProperty(configDbProperties, "opengamma.config.jdbc.username", configPropertyFile);
    String password = getProperty(configDbProperties, "opengamma.config.jdbc.password", configPropertyFile);
    String dbhelper = getProperty(configDbProperties, "opengamma.config.db.dbhelper", configPropertyFile);
    String scheme = getProperty(configDbProperties, "opengamma.config.db.configmaster.scheme", configPropertyFile);
    
    BoneCPDataSource cfgDataSource = new BoneCPDataSource();
    cfgDataSource.setDriverClass(driver);
    cfgDataSource.setJdbcUrl(url);
    cfgDataSource.setUsername(username);
    cfgDataSource.setPassword(password);
    cfgDataSource.setPartitionCount(2);
    cfgDataSource.setMaxConnectionsPerPartition(5);
    cfgDataSource.setAcquireIncrement(1);
    
    DbConnectorFactoryBean connectorFactory = new DbConnectorFactoryBean();
    connectorFactory.setTransactionIsolationLevelName("ISOLATION_SERIALIZABLE");
    connectorFactory.setTransactionPropagationBehaviorName("PROPAGATION_REQUIRED");
    connectorFactory.setName("BatchJobRunnerConfig");
    connectorFactory.setDialectName(dbhelper);
    connectorFactory.setDataSource(cfgDataSource);
    connectorFactory.afterPropertiesSet();
    DbConnector dbConnector = connectorFactory.getObject();
    
    ConfigMaster configMaster = createConfigMaster(managerClass, dbConnector, scheme);
    
    String springContextFile;
    BatchJobParameters parameters = null;

    if (line.hasOption("springXml")) {
      springContextFile = line.getOptionValue("springXml");
    } else {
      ConfigSearchRequest<BatchJobParameters> request = new ConfigSearchRequest<BatchJobParameters>();
      request.setType(BatchJobParameters.class);
      request.setName(configName);
      ConfigSearchResult<BatchJobParameters> searchResult = configMaster.search(request);
      if (searchResult.getValues().size() != 1) {
        throw new IllegalStateException("No unique document with name " + configName + " found - search result was: " + searchResult.getValues());      
      }
      parameters = searchResult.getFirstValue();
      springContextFile = parameters.getSpringXml();
    }

    ApplicationContext context = new FileSystemXmlApplicationContext(springContextFile);
    CommandLineBatchJob job = (CommandLineBatchJob) context.getBean("batchJob");
    
    job.setConfigSource(new MasterConfigSource(configMaster));

    try {
      job.initialize(line, parameters);
    } catch (Exception e) {
      s_logger.error("Failed to initialize BatchJob", e);
      System.exit(-1);
    }

    job.execute();

    boolean failed = false;
    for (BatchJobRun run : job.getRuns()) {
      if (run.isFailed()) {
        failed = true;
      }
    }
    
    if (failed) {
      s_logger.error("Batch failed.");
      System.exit(-1);
    } else {
      s_logger.info("Batch succeeded.");
      System.exit(0);
    }
  }

  private static ConfigMaster createConfigMaster(final String managerClass, final DbConnector dbConnector, final String scheme) throws Exception {
    // this isn't ideal, but moving the code to MasterDB seems to be worse
    Class<? extends ConfigMaster> cls = BatchJobRunner.class.getClassLoader().loadClass(managerClass).asSubclass(ConfigMaster.class);
    ConfigMaster master = cls.getConstructor(DbConnector.class).newInstance(dbConnector);
    master.getClass().getMethod("setIdentifierScheme", String.class).invoke(master, scheme);
    return master;
  }

  private static String getProperty(Properties properties, String propertyName, String configPropertysFile) {
    String property = properties.getProperty(propertyName);
    if (property == null) {
      s_logger.error("Cannot find property " + propertyName + " in " + configPropertysFile);            
      System.exit(-1);
    }
    return property;
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java [-DbatchJob.properties={property file}] com.opengamma.financial.batch.BatchJobRunner [options] {name of config}", CommandLineBatchJob.getOptions());
  }

}
