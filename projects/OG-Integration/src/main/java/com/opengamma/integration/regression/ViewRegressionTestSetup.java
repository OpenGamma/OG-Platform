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

  /* package */ ViewRegressionTestSetup(String dbDumpDir,
                                        String serverConfigFile,
                                        String dbPropertiesFile,
                                        String logbackConfig,
                                        String projectName,
                                        String version,
                                        String workingDirName) {
    ArgumentChecker.notEmpty(dbDumpDir, "databaseDumpDir");
    ArgumentChecker.notEmpty(serverConfigFile, "serverConfigFile");
    ArgumentChecker.notEmpty(logbackConfig, "logbackConfig");
    _dbDumpDir = dbDumpDir;
    _workingDirName = workingDirName;
    _serverConfigFile = serverConfigFile;
    _logbackConfig = logbackConfig;
    _classpath = "config:lib/" + projectName + "-" + version + ".jar";
    try {
      _dbProps.load(new BufferedInputStream(new FileInputStream(dbPropertiesFile)));
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to load properties", e);
    }
  }

  /* package */ void run() {
    RegressionUtils.createEmptyDatabase(_serverConfigFile, _workingDirName, _classpath, _logbackConfig);
    try (ServerProcess ignored = ServerProcess.start(_workingDirName, _classpath, _serverConfigFile, _dbProps, _logbackConfig)) {
      RegressionUtils.restoreDatabase(_workingDirName, _classpath, _dbProps, _serverConfigFile, _logbackConfig, _dbDumpDir);
    }
    // the server needs to be stopped and restarted after the restore so the function configs are loaded
    ServerProcess.start(_workingDirName, _classpath, _serverConfigFile, _dbProps, _logbackConfig);
  }
}
