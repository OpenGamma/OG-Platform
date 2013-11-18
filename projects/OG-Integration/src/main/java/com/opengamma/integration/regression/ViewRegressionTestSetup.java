/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates an empty HSQL database, loads data into it from Fudge XML files and starts a server pointing to it.
 * This is intended to allow a user to add new regression test data to the database before dumping the data back out
 * and committing it to the regression test repository.
 */
/* package */ class ViewRegressionTestSetup {

  private final String _dbDumpDir;
  private final String _serverConfigFile;
  private final String _logbackConfig;
  private final String _classpath;
  private final String _workingDirName;
  private final Properties _dbProps = new Properties();
  private final String _dbPropertiesFile;

  /* package */ ViewRegressionTestSetup(String dbDumpDir,
                                        String serverConfigFile,
                                        String dbPropertiesFile,
                                        String logbackConfig,
                                        String projectName,
                                        String version,
                                        String workingDirName) {
    _dbPropertiesFile = ArgumentChecker.notEmpty(dbPropertiesFile, "dbPropertiesFile");
    _dbDumpDir = ArgumentChecker.notEmpty(dbDumpDir, "dbDumpDir");
    _workingDirName = ArgumentChecker.notEmpty(workingDirName, "workingDirName");
    _serverConfigFile = ArgumentChecker.notEmpty(serverConfigFile, "serverConfigFile");
    _logbackConfig = ArgumentChecker.notEmpty(logbackConfig, "logbackConfig");
    // TODO will this work on windows?
    _classpath = "config:lib/" + projectName + "-" + version + ".jar";
    try {
      _dbProps.load(new BufferedInputStream(new FileInputStream(dbPropertiesFile)));
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to load properties", e);
    }
  }

  /* package */ void run() {
    RegressionUtils.createEmptyDatabase(_dbPropertiesFile, _workingDirName, _classpath, _logbackConfig);
    RegressionUtils.restoreDatabase(_workingDirName, _classpath, _dbProps, _serverConfigFile, _logbackConfig, _dbDumpDir);
    ServerProcess.start(_workingDirName, _classpath, _serverConfigFile, _dbProps, _logbackConfig);
  }
}
