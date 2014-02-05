/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 * Historical timeseries snapshotter.
 */
public class HistoricalTimeSeriesSnapshotter extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma JVM process for development.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (args.length == 0) {
      // if no command line arguments, then use default arguments suitable for development in an IDE
      // the first argument is for verbose startup, to aid understanding
      // the second argument defines the start of a chain of properties files providing the configuration
      args = new String[] {"-v", "classpath:/htssnapshot/hts-snapshot.properties"};
    }
    if (!new HistoricalTimeSeriesSnapshotter().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }
}
