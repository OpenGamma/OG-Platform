/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maven.db;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.hsqldb.jdbcDriver;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.maven.MojoUtils;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.HSQLDbDialect;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.HSQLDbManagement;
import com.opengamma.util.db.script.DbScriptDirectory;
import com.opengamma.util.db.script.DbScriptReader;
import com.opengamma.util.db.script.FileDbScriptDirectory;
import com.opengamma.util.db.script.ZipFileDbScriptDirectory;
import com.opengamma.util.db.tool.DbCreateOperation;
import com.opengamma.util.db.tool.DbToolContext;

/**
 * Maven plugin wrapping a {@link DbCreateOperation} to create a local HSQL database.
 * <p>
 * For more advanced or custom database operations, use the associated tools from the command line.
 * 
 * @goal hsqldb-create
 * @requiresDependencyResolution compile
 */
public class HSQLDbCreateMojo extends AbstractMojo {

  /**
   * @parameter alias="dbPath"
   * @required
   */
  private String _dbPath;
  /**
   * @parameter alias="username"
   * @required
   */
  private String _username;
  /**
   * @parameter alias="password"
   * @required
   */
  private String _password;
  /**
   * @parameter alias="scriptsArtifact"
   */
  private String _scriptsArtifact;
  /**
   * @parameter alias="scriptsPath"
   * @required
   */
  private String _scriptsPath;
  
  /**
   * @component
   */
  private MavenProject _project;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    BoneCPDataSource dataSource = new BoneCPDataSource();
    dataSource.setDriverClass(jdbcDriver.class.getName());
    dataSource.setJdbcUrl("jdbc:hsqldb:file:" + _dbPath);
    dataSource.setUsername(_username);
    dataSource.setPassword(_password);
    dataSource.setPoolName("hsqldb");
    dataSource.setPartitionCount(1);
    dataSource.setAcquireIncrement(1);
    dataSource.setMinConnectionsPerPartition(1);
    dataSource.setMaxConnectionsPerPartition(1);
    
    // REVIEW jonathan 2012-10-12 -- workaround for PLAT-2745
    String dbPath = dataSource.getJdbcUrl();
    int lastSlashIdx = dbPath.lastIndexOf("/");
    if (lastSlashIdx == -1) {
      throw new MojoExecutionException("dbPath must contain '/' before the database name");
    }
    String dbHost = dbPath.substring(0, lastSlashIdx);
    String catalog = dbPath.substring(lastSlashIdx + 1);
    
    SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource), new DefaultTransactionDefinition());
    
    DbToolContext dbToolContext = new DbToolContext();
    try {
      DbManagement dbManagement = HSQLDbManagement.getInstance();
      dbManagement.initialise(dbHost, _username, _password);
      dbToolContext.setDbManagement(dbManagement);
      dbToolContext.setDbConnector(new DbConnector("hsqldb", new HSQLDbDialect(), dataSource, jdbcTemplate, null, transactionTemplate));
      dbToolContext.setCatalog(catalog);
      
      DbScriptDirectory scriptsBaseDir;
      if (_scriptsArtifact != null) {
        File scriptsResource = MojoUtils.getArtifactFile(_scriptsArtifact, _project);
        scriptsBaseDir = new ZipFileDbScriptDirectory(scriptsResource, _scriptsPath);
      } else {
        scriptsBaseDir = new FileDbScriptDirectory(new File(_scriptsPath));
      }
      dbToolContext.setScriptReader(new DbScriptReader(scriptsBaseDir));
      new DbCreateOperation(dbToolContext, true, null, true).execute();
      dbToolContext.close();
    } catch (Exception e) {
      throw new MojoExecutionException("Error creating database schemas", e);
    } finally {
      dbToolContext.close();
    }
  }

}
