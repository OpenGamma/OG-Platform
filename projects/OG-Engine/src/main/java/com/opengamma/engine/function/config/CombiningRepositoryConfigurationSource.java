/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combines the repository configuration from two or more sources into a single configuration
 * object.
 */
public class CombiningRepositoryConfigurationSource implements RepositoryConfigurationSource {

  private final RepositoryConfigurationSource[] _sources;

  public CombiningRepositoryConfigurationSource(final RepositoryConfigurationSource... sources) {
    _sources = Arrays.copyOf(sources, sources.length);
  }

  protected RepositoryConfigurationSource[] getSources() {
    return _sources;
  }

  @Override
  public RepositoryConfiguration getRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    for (RepositoryConfigurationSource source : getSources()) {
      configs.addAll(source.getRepositoryConfiguration().getFunctions());
    }
    return new RepositoryConfiguration(configs);
  }

}
