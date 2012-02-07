/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.component;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.OpenGammaComponentServer;

/**
 * Main entry point for OpenGamma component-based servers in OG-Examples.
 * <p>
 * This class exists to easily capture the classpath.
 */
public class ExampleComponentServer extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma JVM process.
   * <p>
   * The command line must specify a configuration file to start.
   * See {@link OpenGammaComponentServer} and {@link ComponentManager}.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleComponentServer().run(args);
  }

}
