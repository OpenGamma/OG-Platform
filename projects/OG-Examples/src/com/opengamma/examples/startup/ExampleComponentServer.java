/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.startup;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.OpenGammaComponentServer;

/**
 * Entry point for OpenGamma component-based servers in OG-Examples that must
 * be passed a configuration file on the command line.
 * <p>
 * This class exists to easily capture the classpath for IDEs.
 * Command lines should just start {@link OpenGammaComponentServer}.
 */
public class ExampleComponentServer {

  /**
   * Main method to start an OpenGamma JVM process.
   * <p>
   * The command line must specify a configuration file to start.
   * See {@link OpenGammaComponentServer} and {@link ComponentManager}.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    new OpenGammaComponentServer().run(args);
  }

}
