/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upgrades database schemas using the installation scripts.
 */
public class DbSchemaMigrateOperation extends AbstractDbSchemaScriptOperation<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSchemaMigrateOperation.class);
  
  /**
   * Constructs an instance.
   * 
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   */
  public DbSchemaMigrateOperation(DbToolContext dbToolContext, boolean write, File outputFile) {
    super(dbToolContext, write, outputFile);
  }
  
  @Override
  public void execute() {
    SqlScriptWriter writer = createSqlScriptWriter();
    try {
      for (String schemaGroup : getDbToolContext().getSchemaGroups()) {
        List<File> sqlScripts = getSchemaMigrateScripts(schemaGroup);
        if (sqlScripts == null) {
          s_logger.info(schemaGroup + " does not support migration");
          continue;
        }
        if (sqlScripts.isEmpty()) {
          s_logger.info(schemaGroup + " already at latest version");
          continue;
        }
        s_logger.info(schemaGroup + " is behind by " + sqlScripts.size() + " versions");
        for (int i = 0; i < sqlScripts.size(); i++) {
          File sqlScript = sqlScripts.get(i);
          s_logger.debug("Using schema migration file: " + sqlScript);
          writer.write(schemaGroup + " - " + (i + 1) + " of " + sqlScripts.size(), sqlScript);
        }
      }
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        s_logger.error("Error closing SQL script writer", e);
      }
    }
  }

}
