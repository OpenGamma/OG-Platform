/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ConfigurationScanner} combines the outputs from other scanners.
 */
public class CombiningConfigurationScanner extends AbstractConfigurationScanner {

  private final AtomicInteger _pending = new AtomicInteger(1);

  /**
   * Adds a configuration scanner to this combining one. Do not call after calling {@link #start}.
   * 
   * @param scanner the scanner to add, not null
   */
  public void addConfigurationScanner(final ConfigurationScanner scanner) {
    _pending.incrementAndGet();
    scanner.addListener(new ConfigurationListener() {

      @Override
      public void foundConfigurations(final Set<Configuration> configurations, final boolean complete) {
        addConfigurations(configurations);
        if (complete) {
          if (_pending.decrementAndGet() == 0) {
            complete();
          }
        }
      }

    });
  }

  /**
   * Marks the scanner as started - do not call {@link #addConfigurationScanner} afterwards.
   */
  public void start() {
    if (_pending.decrementAndGet() == 0) {
      complete();
    }
  }

}
