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
 * Upgrades or Creates database objects using the installation scripts.
 */
public class DbUpgradeOrCreateOperation extends AbstractDbScriptOperation<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbUpgradeOrCreateOperation.class);

  private boolean _upgradeRequired;

  /**
   * Constructs an instance.
   *
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the commands that would be run
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   */
  public DbUpgradeOrCreateOperation(DbToolContext dbToolContext, boolean write, File outputFile) {
    super(dbToolContext, write, outputFile);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets whether an upgrade was found to be required on the last execution.
   * 
   * @return true if an upgrade was required, false otherwise
   */
  public boolean isUpgradeRequired() {
    return _upgradeRequired;
  }

  private void setUpgradeRequired(boolean upgradeRequired) {
    _upgradeRequired = upgradeRequired;
  }

  //-------------------------------------------------------------------------
  @Override
  public void execute() {
    SqlScriptWriter writer = createSqlScriptWriter();
    boolean upgradeRequired = false;
    try {
      Set<String> schemaNames = getDbToolContext().getSchemaNames() != null ? getDbToolContext().getSchemaNames() : getAllSchemaNames();
      for (String schema : schemaNames) {
        Integer currentVersion = getCurrentGroupVersion(schema);
        if (currentVersion == null) {
          //craete
          s_logger.info("Processing schema " + schema);
          DbScript script = getCreationScript(schema);
          s_logger.debug("Using script: " + script);
          writer.write(schema, script);
        } else {
          //update
          List<DbScript> scripts = getMigrationScripts(schema);
          if (scripts == null) {
            s_logger.info(schema + " does not support migration");
            continue;
          }
          if (scripts.isEmpty()) {
            s_logger.info(schema + " already at latest version");
            continue;
          }
          upgradeRequired = true;
          s_logger.info(schema + " is behind by " + scripts.size() + " versions");
          for (int i = 0; i < scripts.size(); i++) {
            DbScript script = scripts.get(i);
            s_logger.debug("Using schema migration file: " + script);
            writer.write(schema + " - " + (i + 1) + " of " + scripts.size(), script);
          }
        }
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error processing creation/migration scripts", e);
    } finally {
      setUpgradeRequired(upgradeRequired);
      try {
        writer.close();
      } catch (IOException e) {
        s_logger.error("Error closing SQL script writer", e);
      }
    }
  }

}
