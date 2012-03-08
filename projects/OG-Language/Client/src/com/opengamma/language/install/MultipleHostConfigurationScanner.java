/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.install;

import java.util.concurrent.ExecutorService;

import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ConfigurationScanner} that scans a set of hosts.
 */
public class MultipleHostConfigurationScanner extends CombiningConfigurationScanner {

  private final ExecutorService _executor;

  public MultipleHostConfigurationScanner(final ExecutorService executor) {
    ArgumentChecker.notNull(executor, "executor");
    _executor = executor;
  }

  /**
   * Adds a host to the set to scan. Do not call after calling {@link #start}.
   * 
   * @param host the host name
   */
  public void addHost(final String host) {
    addHostConfigurationScanner(new HostConfigurationScanner(host));
  }

  /**
   * Adds a host to the set to scan. Do not call after calling {@link #start}.
   * 
   * @param host the host name
   * @param port the host port
   */
  public void addHost(final String host, final int port) {
    addHostConfigurationScanner(new HostConfigurationScanner(host, port));
  }

  /**
   * Adds a host to the set to scan. Do not call after calling {@link #start}.
   * 
   * @param host the host name
   * @param port the host port
   * @param base the host base URL
   */
  public void addHost(final String host, final int port, final String base) {
    addHostConfigurationScanner(new HostConfigurationScanner(host, port, base));
  }

  /**
   * Adds a host to the set to scan. Do not call after calling {@link #start}.
   * 
   * @param host the host name
   * @param base the host base URL
   */
  public void addHost(final String host, final String base) {
    addHostConfigurationScanner(new HostConfigurationScanner(host, base));
  }

  protected ExecutorService getExecutor() {
    return _executor;
  }

  protected void addHostConfigurationScanner(final HostConfigurationScanner scanner) {
    addConfigurationScanner(scanner);
    getExecutor().submit(scanner);
  }

}
