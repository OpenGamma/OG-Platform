/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.install.ConfigurationScanner.ConfigurationListener;

/**
 * Utility class for simple query of configuration URL information.
 */
public final class HostConfigurationUtils {

  private static final int TIMEOUT = 30;

  private HostConfigurationUtils() {
  }

  private static String[] splitHosts(final String hostexpr) {
    return hostexpr.split(",\\s*");
  }

  /**
   * Produces a list of possible configurations. A single host can be given, or multiple
   * hosts can be given as a comma separated list. The call will block until the scan is
   * complete. For a non-blocking form the scanner interfaces must be used directly.
   * 
   * @param hostexpr the host to scan, or multiple host names separated by commas
   * @return the configurations found, or an empty list if none
   */
  public static List<Configuration> getConfiguration(final String hostexpr) {
    final MultipleHostConfigurationScanner scanner = new MultipleHostConfigurationScanner(Executors.newCachedThreadPool());
    for (String host : splitHosts(hostexpr)) {
      // TODO: check if the host was given as "host:port"
      scanner.addHost(host);
    }
    scanner.start();
    return getConfiguration(scanner);
  }

  protected static List<Configuration> getConfiguration(final ConfigurationScanner scanner) {
    final LinkedBlockingQueue<List<Configuration>> result = new LinkedBlockingQueue<List<Configuration>>();
    scanner.addListener(new ConfigurationListener() {

      @Override
      public void foundConfigurations(final Set<Configuration> configurations, final boolean complete) {
        if (complete) {
          result.add(new ArrayList<Configuration>(configurations));
        }
      }

    });
    try {
      final List<Configuration> results = result.poll(TIMEOUT, TimeUnit.SECONDS);
      if (results == null) {
        throw new OpenGammaRuntimeException("Exceeded " + TIMEOUT + "s timeout waiting for configuration results");
      }
      Collections.sort(results, Configuration.SORT_BY_HOST);
      return results;
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("interrupted", e);
    }
  }

}
