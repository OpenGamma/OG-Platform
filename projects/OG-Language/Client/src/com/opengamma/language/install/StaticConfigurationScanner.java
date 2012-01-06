/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Trivial implementation of {@link ConfigurationScanner} that returns a static set.
 */
public class StaticConfigurationScanner implements ConfigurationScanner {

  private final Set<Configuration> _configurations;

  public StaticConfigurationScanner(final Collection<Configuration> configurations) {
    ArgumentChecker.notNull(configurations, "configurations");
    _configurations = Collections.unmodifiableSet(new HashSet<Configuration>(configurations));
  }

  @Override
  public Set<Configuration> getConfigurations() {
    return _configurations;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public void addListener(final ConfigurationListener listener) {
    listener.foundConfigurations(getConfigurations(), true);
  }

  @Override
  public void removeListener(final ConfigurationListener listener) {
    // No-op
  }

}
