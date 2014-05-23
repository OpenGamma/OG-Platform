/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 * Entry point for the OpenGamma component-based server in Examples-Simulated automatically
 * starting the "development" configuration file.
 * <p>
 * This file is intended for use with an IDE and a checked out source code tree.
 * It relies on the OG-Web directory being relative to Examples-Simulated in the file
 * system as per a standard checkout of OG-Platform.
 * The server will not start correctly if you have obtained a bundle in distribution format.
 * <p>
 * Before running this class, you must have setup the example HSQL database.
 * To do this, run the Ant tasks "new-hsqldb" and "init-database".
 * <p>
 * Command lines should just start {@link OpenGammaComponentServer}.
 */
public class ExamplesSimulatedComponentServerDev extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma JVM process for development.
   * <p>
   * If the command line is empty, the "development" configuration file is started.
   * This file is intended for use with an IDE and a checked out source code tree.
   * It relies on the OG-Web directory being relative to Examples-Simulated in the file
   * system as per a standard checkout of OG-Platform.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (args.length == 0) {
      // if no command line arguments, then use default arguments suitable for development in an IDE
      // the first argument is for verbose startup, to aid understanding
      // the second argument defines the start of a chain of properties files providing the configuration
      args = new String[] {"-v", "classpath:/fullstack/fullstack-examplessimulated-dev.properties"};
    }
    if (!new ExamplesSimulatedComponentServerDev().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }

}
