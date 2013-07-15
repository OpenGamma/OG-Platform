/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

/**
 * Creates a local ViewProcessor and connects to it to run the demo.
 */
public class LocalEngineViewerLauncher extends AbstractEngineViewerLauncher {

  @Override
  @SuppressWarnings("deprecation")
  protected void startup() {
    DemoViewProcessor setup = new DemoViewProcessor();
    startViewer(setup.getViewProcessor());
  }

  /**
   * Starts the demo.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) { // CSIGNORE
    launch(LocalEngineViewerLauncher.class, args);
  }

}
