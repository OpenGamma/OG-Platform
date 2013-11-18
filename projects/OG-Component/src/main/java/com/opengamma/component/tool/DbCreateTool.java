/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.File;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
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
  /**
   * A comma-separated list of schema groups to filter by.
   */
  private static final String SCHEMA_NAME_FILTER = "s";
  
  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption(createDropExistingOption());
    options.addOption(createIncludeSchemaGroupOption());
    return options;
  }
  
  private static Option createDropExistingOption() {
    return new Option(DROP_EXISTING_OPTION, "drop", false, "whether to drop any existing contents");
  }
  
  private static Option createIncludeSchemaGroupOption() {
    Option schemaGroupOption = new Option(SCHEMA_NAME_FILTER, "schema-name", false, "a schema group name to include; includes all schemas if none specified");
    schemaGroupOption.setArgs(Option.UNLIMITED_VALUES);
    return schemaGroupOption;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun(boolean write, File outputFile) throws Exception {
    boolean dropExisting = getCommandLine().hasOption(DROP_EXISTING_OPTION);
    if (getCommandLine().hasOption(SCHEMA_NAME_FILTER)) {
      String[] nameArgs = getCommandLine().getOptionValues(SCHEMA_NAME_FILTER);
      Set<String> includeNames = ImmutableSet.copyOf(nameArgs);
      Set<String> allNames = getDbToolContext().getSchemaNames();
      SetView<String> filteredNames = Sets.intersection(allNames, includeNames);
      if (write) {
        System.out.println("Filtered " + allNames + " to " + filteredNames);
      }
      getDbToolContext().setSchemaNames(filteredNames);
    }
    if (getDbToolContext().getSchemaNames().isEmpty()) {
      System.out.println("No schemas specified, or all filtered out");
    } else {
      new DbCreateOperation(getDbToolContext(), write, outputFile, dropExisting).execute();
      if (write) {
        System.out.println("Database objects created successfully");
      }
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
