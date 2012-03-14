/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.startup;

import java.io.IOException;

import com.opengamma.component.OpenGammaComponentServer;

/**
 * Starts the full-stack masters and engine.
 */
public class EngineServer {

  public static void main(String[] args) throws IOException { // CSIGNORE
    if (args.length == 0) {
      args = new String[] {"-v", "classpath:fullstack/fullstack-shareddev.properties" };
    }
    new OpenGammaComponentServer().run(args);
  }

}
