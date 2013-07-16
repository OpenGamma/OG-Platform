/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.scripts.Scriptable;
import com.opengamma.util.db.tool.DbCreateOperation;
import com.opengamma.util.db.tool.DbToolContext;

/**
 * Tool for creating database objects using the installation scripts.
 */
@Scriptable
public class DbCreateTool extends AbstractDbTool<DbToolContext> {
  
  /**
   * Drop existing contents command line option.
   */
  private static final String DROP_EXISTING_OPTION = "d";
  
  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption(createDropExistingOption());
    return options;
  }
  
  private static Option createDropExistingOption() {
    return new Option(DROP_EXISTING_OPTION, "drop", false, "whether to drop any existing contents");
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun(boolean write, File outputFile) throws Exception {
    boolean dropExisting = getCommandLine().hasOption(DROP_EXISTING_OPTION);
    new DbCreateOperation(getDbToolContext(), write, outputFile, dropExisting).execute();
    if (write) {
      System.out.println("Database objects created successfully");
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    boolean success = new DbCreateTool().initAndRun(args, DbToolContext.class);
    System.exit(success ? 0 : 1);
  }

}
