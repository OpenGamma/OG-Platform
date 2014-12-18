/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.server;

import com.opengamma.component.OpenGammaComponentServer;

/**
 * Hard coded example server entry point
 */
public class OpenGammaServer {

  public static void main(String[] args) {
    String[] config = {"classpath:fullstack/fullstack-in-memory.properties"};
    new OpenGammaComponentServer().run(config);

  }

}
