/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.DbSourceFactoryBean;

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
      line = parser.parse(BatchJob.getOptions(), args);
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
    
    BasicDataSource cfgDataSource = new BasicDataSource();
    cfgDataSource.setDriverClassName(driver);
    cfgDataSource.setUrl(url);
    cfgDataSource.setUsername(username);
    cfgDataSource.setPassword(password);
    
    DbSourceFactoryBean dbSourceFactory = new DbSourceFactoryBean();
    dbSourceFactory.setTransactionIsolationLevelName("ISOLATION_SERIALIZABLE");
    dbSourceFactory.setTransactionPropagationBehaviorName("PROPAGATION_REQUIRED");
    dbSourceFactory.setName("BatchJobRunnerConfig");
    dbSourceFactory.setDialect(dbhelper);
    dbSourceFactory.setDataSource(cfgDataSource);
    
    dbSourceFactory.afterPropertiesSet();
    DbSource dbSource = dbSourceFactory.getObject();
    
    ConfigMaster configMaster = createConfigMaster(managerClass, dbSource, scheme);
    
    String springContextFile;
    BatchJobParameters parameters = null;

    if (line.hasOption("springXml")) {
      springContextFile = line.getOptionValue("springXml");
    } else {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(configName);
      ConfigSearchResult<BatchJobParameters> searchResult = configMaster.typed(BatchJobParameters.class).search(request);
      if (searchResult.getValues().size() != 1) {
        throw new IllegalStateException("No unique document with name " + configName + " found - search result was: " + searchResult.getValues());      
      }
      parameters = searchResult.getFirstValue();
      springContextFile = parameters.getSpringXml();
    }

    ApplicationContext context = new FileSystemXmlApplicationContext(springContextFile);
    BatchJob job = (BatchJob) context.getBean("batchJob");
    
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

  private static ConfigMaster createConfigMaster(final String managerClass, final DbSource dbSource, final String scheme) throws Exception {
    // this isn't ideal, but moving the code to MasterDB seems to be worse
    Class<? extends ConfigMaster> cls = BatchJobRunner.class.getClassLoader().loadClass(managerClass).asSubclass(ConfigMaster.class);
    ConfigMaster master = cls.getConstructor(DbSource.class).newInstance(dbSource);
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
    formatter.printHelp("java [-DbatchJob.properties={property file}] com.opengamma.financial.batch.BatchJobRunner [options] {name of config}", BatchJob.getOptions());
  }

}
