/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import com.opengamma.component.OpenGammaComponentServer;

/**
 * Extremely simple class that does nothing but delegate to {@link OpenGammaComponentServer} and outputs to stdout
 * so a controlling process can monitor the state.
 */
public final class StdoutComponentServer {

  /** String printed to stdout when startup begins. */
  public static final String STARTING = "STARTING";
  /** String printed to stdout if startup fails. */
  public static final String STARTUP_FAILED = "STARTUP_FAILED";
  /** String printed to stdout when startup is complete. */
  public static final String STARTUP_COMPLETE = "STARTUP_COMPLETE";

  private StdoutComponentServer() {
  }

  public void run(String[] args) {
    System.out.println(STARTING);
    boolean success = new OpenGammaComponentServer().run(args);
    System.out.println(success ? STARTUP_COMPLETE : STARTUP_FAILED);
  }
}
