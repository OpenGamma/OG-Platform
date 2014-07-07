/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.ResourceUtils;

/**
 * Tool class that creates and initializes the example database.
 */
@Scriptable
public class ExampleDatabaseCreator extends ExampleDatabaseInit {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabaseCreator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * <p>
   * If the command line is empty, the "development" configuration file is started.
   * This file is intended for use with an IDE and a checked out source code tree.
   *
   * @param args the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleDatabaseCreator().runInstance(args);
  }

  //-------------------------------------------------------------------------
  @Override
  public final void run(String configFile) throws Exception {
    super.run(configFile);
    Resource res = ResourceUtils.createResource(configFile);
    // populate the database
    s_logger.warn("Populating main database...");
    new ExampleDatabasePopulator().run(ResourceUtils.toResourceLocator(res), ToolContext.class);
    s_logger.warn("Successfully created example databases");
  }

}
