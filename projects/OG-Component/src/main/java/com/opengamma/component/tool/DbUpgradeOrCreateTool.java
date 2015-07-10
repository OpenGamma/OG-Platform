/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.File;

import com.opengamma.scripts.Scriptable;
import com.opengamma.util.db.tool.DbToolContext;
import com.opengamma.util.db.tool.DbUpgradeOperation;
import com.opengamma.util.db.tool.DbUpgradeOrCreateOperation;

/**
 * Tool for upgrading database objects to the latest version using the installation scripts.
 */
@Scriptable
public class DbUpgradeOrCreateTool extends AbstractDbTool<DbToolContext> {

  //-------------------------------------------------------------------------
  @Override
  protected void doRun(boolean write, File outputFile) throws Exception {
    DbUpgradeOrCreateOperation upgradeOp = new DbUpgradeOrCreateOperation(getDbToolContext(), write, outputFile);
    upgradeOp.execute();
    if (!upgradeOp.isUpgradeRequired()) {
      System.out.println("Database up-to-date");
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    boolean success = new DbUpgradeOrCreateTool().initAndRun(args, DbToolContext.class);
    System.exit(success ? 0 : 1);
  }

}
