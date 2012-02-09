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
 * This class exists to easily capture the classpath for IDEs.
 * Command lines should just start {@link OpenGammaComponentServer}.
 */
public class ExampleComponentServerDev {

  /**
   * Main method to start an OpenGamma JVM process.
   * <p>
   * If the command line is empty, the "development" configuration file is started.
   * This file is intended for use with a checked out source code tree, as it relies
   * on the OG-Web directory being alongside OG-Examples.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) throws Exception { // CSIGNORE
    if (args.length == 0) {
      args = new String[] {"-v", "classpath:fullstack/fullstack-example-dev.properties"};
    }
    new OpenGammaComponentServer().run(args);
  }

}
