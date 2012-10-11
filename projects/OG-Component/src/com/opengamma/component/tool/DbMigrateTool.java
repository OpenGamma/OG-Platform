/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.File;

import com.opengamma.util.db.tool.DbMigrateOperation;
import com.opengamma.util.db.tool.DbToolContext;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Tool for migrating database objects to the latest version using the installation scripts.
 */
@Scriptable
public class DbMigrateTool extends AbstractDbTool<DbToolContext> {

  //-------------------------------------------------------------------------
  @Override
  protected void doRun(boolean write, File outputFile) throws Exception {
    new DbMigrateOperation(getDbToolContext(), write, outputFile).execute();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    boolean success = new DbMigrateTool().initAndRun(args, DbToolContext.class);
    System.exit(success ? 0 : 1);
  }

}
