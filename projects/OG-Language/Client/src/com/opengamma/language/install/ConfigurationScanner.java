/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.Set;

/**
 * Scanner that delivers available configurations.
 */
public interface ConfigurationScanner {

  // TODO: this is probably in the wrong place; it would be useful for configuring remote calc nodes too

  /**
   * Callback interface to receive asynchronous updates of configurations (as an alternative
   * to polling the {@link #getConfigurations} method.
   */
  interface ConfigurationListener {

    /**
     * Called when a set of configurations is available.
     * 
     * @param configurations the configurations found
     * @param complete true if this is the full set, false if it is an intermediate result
     */
    void foundConfigurations(Set<Configuration> configurations, boolean complete);

  }

  /**
   * Retrieves the current set of configurations found.
   * 
   * @return the current set of configurations found
   */
  Set<Configuration> getConfigurations();

  /**
   * Tests if the configurations returned by the next call to {@link #getConfigurations} will
   * be the complete set or if it may change. If the scan is not complete, either a listener
   * can be registered or {@link #getConfigurations} polled.
   * 
   * @return True if the scan is complete, false if there are still actions pending
   */
  boolean isComplete();

  /**
   * Adds a listener to receive notification of new configurations found. The listener will be
   * called at least once with the set of configurations.
   * 
   * @param listener the listener to add, not null
   */
  void addListener(ConfigurationListener listener);

  /**
   * Removes a previously registered listener for new configurations.
   * 
   * @param listener the listener to remove, not null
   */
  void removeListener(ConfigurationListener listener);

}
