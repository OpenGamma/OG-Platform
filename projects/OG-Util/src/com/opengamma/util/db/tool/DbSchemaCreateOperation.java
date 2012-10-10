/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates database schemas using the installation scripts.
 */
public class DbSchemaCreateOperation extends AbstractDbSchemaScriptOperation<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSchemaCreateOperation.class);
  
  private final boolean _dropExisting;
  
  /**
   * Constructs an instance.
   * 
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   * @param dropExisting  indicates whether to drop any existing contents before attempting to create the schemas 
   */
  public DbSchemaCreateOperation(DbToolContext dbToolContext, boolean write, File outputFile, boolean dropExisting) {
    super(dbToolContext, write, outputFile);
    _dropExisting = dropExisting;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void execute() {
    if (isDropExisting()) {
      dropSchema();
    }
    SqlScriptWriter writer = createSqlScriptWriter();
    try {
      for (String schemaGroup : getDbToolContext().getSchemaGroups()) {
        s_logger.info("Processing schema group " + schemaGroup);
        File sqlScript = getLatestSchemaCreateScript(schemaGroup);
        s_logger.debug("Using schema creation file: " + sqlScript);
        writer.write(schemaGroup, sqlScript);
      }
      s_logger.info("Scripts processed successfully");
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        s_logger.error("Error closing SQL script writer", e);
      }
    }
  }
  
  protected void dropSchema() {
    contextNotNull(getDbToolContext().catalog());
    
    // Give the user a chance to kill the script
    s_logger.warn("About to erase the contents of " + getDbToolContext().getCatalog() + "...");
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }
    
    s_logger.info("Dropping contents of catalog " + getDbToolContext().getCatalog());
    getDbToolContext().getDbManagement().dropSchema(getDbToolContext().getCatalog(), null);
  }
  
  //-------------------------------------------------------------------------
  private boolean isDropExisting() {
    return _dropExisting;
  }
  
}
