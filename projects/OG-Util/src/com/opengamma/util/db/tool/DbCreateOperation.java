/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.db.script.DbScript;

/**
 * Creates database objects using the installation scripts.
 */
public class DbCreateOperation extends AbstractDbScriptOperation<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbCreateOperation.class);
  
  private final boolean _dropCatalog;
  
  /**
   * Constructs an instance.
   * 
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   * @param dropCatalog  indicates whether to drop the catalog if it already exists 
   */
  public DbCreateOperation(DbToolContext dbToolContext, boolean write, File outputFile, boolean dropCatalog) {
    super(dbToolContext, write, outputFile);
    _dropCatalog = dropCatalog;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void execute() {
    if (isDropCatalog()) {
      dropCatalog();
    }
    SqlScriptWriter writer = createSqlScriptWriter();
    try {
      Set<String> groups = getDbToolContext().getGroups() != null ? getDbToolContext().getGroups() : getAllGroupNames();
      for (String group : groups) {
        s_logger.info("Processing group " + group);
        DbScript script = getCreationScript(group);
        s_logger.debug("Using script: " + script);
        writer.write(group, script);
      }
      s_logger.info("Scripts processed successfully");
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error processing creation scripts", e);
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        s_logger.error("Error closing SQL script writer", e);
      }
    }
  }
  
  protected void dropCatalog() {
    contextNotNull(getDbToolContext().catalog());
    
    if (!isWrite()) {
      s_logger.info("Would erase the contents of " + getDbToolContext().getCatalog() + " but skipping in read-only mode");
      return;
    }
        
    if (s_logger.isInfoEnabled()) {
      // Give the user a chance to kill the script
      s_logger.info("About to erase the contents of " + getDbToolContext().getCatalog() + "...");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
      }
    }
    
    s_logger.info("Dropping contents of catalog " + getDbToolContext().getCatalog());
    getDbToolContext().getDbManagement().dropSchema(getDbToolContext().getCatalog(), null);
  }
  
  //-------------------------------------------------------------------------
  private boolean isDropCatalog() {
    return _dropCatalog;
  }
  
}
