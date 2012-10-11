/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.db.script.DbScript;

/**
 * Upgrades database objects using the installation scripts.
 */
public class DbMigrateOperation extends AbstractDbScriptOperation<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbMigrateOperation.class);
  
  /**
   * Constructs an instance.
   * 
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   */
  public DbMigrateOperation(DbToolContext dbToolContext, boolean write, File outputFile) {
    super(dbToolContext, write, outputFile);
  }
  
  @Override
  public void execute() {
    SqlScriptWriter writer = createSqlScriptWriter();
    try {
      Set<String> schemaGroups = getDbToolContext().getGroups() != null ? getDbToolContext().getGroups() : getAllGroupNames();
      for (String schemaGroup : schemaGroups) {
        List<DbScript> scripts = getMigrationScripts(schemaGroup);
        if (scripts == null) {
          s_logger.info(schemaGroup + " does not support migration");
          continue;
        }
        if (scripts.isEmpty()) {
          s_logger.info(schemaGroup + " already at latest version");
          continue;
        }
        s_logger.info(schemaGroup + " is behind by " + scripts.size() + " versions");
        for (int i = 0; i < scripts.size(); i++) {
          DbScript script = scripts.get(i);
          s_logger.debug("Using schema migration file: " + script);
          writer.write(schemaGroup + " - " + (i + 1) + " of " + scripts.size(), script);
        }
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error processing migration scripts", e);
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        s_logger.error("Error closing SQL script writer", e);
      }
    }
  }

}
