/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.startup;

import com.opengamma.component.OpenGammaComponentServer;

/**
 * Entry point for the OpenGamma component-based server in OG-Examples automatically
 * starting the "development" configuration file.
 * <p>
 * This file is intended for use with an IDE and a checked out source code tree.
 * It relies on the OG-Web directory being alongside OG-Examples in the file system.
 * <p>
 * Command lines should just start {@link OpenGammaComponentServer}.
 */
public class ExampleComponentServerDev extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma JVM process for development.
   * <p>
   * If the command line is empty, the "development" configuration file is started.
   * This file is intended for use with an IDE and a checked out source code tree.
   * It relies on the OG-Web directory being alongside OG-Examples in the file system.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (args.length == 0) {
      args = new String[] {"-v", "classpath:fullstack/fullstack-example-dev.properties"};
    }
    new ExampleComponentServerDev().run(args);
  }

}
