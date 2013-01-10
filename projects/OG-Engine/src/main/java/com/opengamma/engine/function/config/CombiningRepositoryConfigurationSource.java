/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Combines the repository configuration from two or more sources into a single configuration
 * object.
 */
public class CombiningRepositoryConfigurationSource implements RepositoryConfigurationSource {

  private final RepositoryConfigurationSource[] _sources;

  protected CombiningRepositoryConfigurationSource(final RepositoryConfigurationSource[] sources) {
    _sources = sources;
  }

  private static int count(final RepositoryConfigurationSource source) {
    if (source == null) {
      return 0;
    } else if (source instanceof CombiningRepositoryConfigurationSource) {
      int count = 0;
      for (final RepositoryConfigurationSource inner : ((CombiningRepositoryConfigurationSource) source).getSources()) {
        count += count(inner);
      }
      return count;
    } else {
      return 1;
    }
  }

  private static int copy(final RepositoryConfigurationSource source, final RepositoryConfigurationSource[] dest, int index) {
    if (source != null) {
      if (source instanceof CombiningRepositoryConfigurationSource) {
        for (final RepositoryConfigurationSource inner : ((CombiningRepositoryConfigurationSource) source).getSources()) {
          index = copy(inner, dest, index);
        }
      } else {
        dest[index++] = source;
      }
    }
    return index;
  }

  /**
   * Creates an instance that is the union of the given sources. The sources are not queried for their configuration until the composite object get queried.
   * <p>
   * Any nulls in the combined sources are ignored. Any sources that are themselves {@code CombinedRepositoryConfigurationSource} instances are expanded and their members used directly in the
   * composite.
   *
   * @param sources the sources to combine, may contain nulls
   * @return the composite source, not null
   */
  public static RepositoryConfigurationSource of(final RepositoryConfigurationSource... sources) {
    if ((sources == null) || (sources.length == 0)) {
      return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
    }
    int i = 0;
    for (final RepositoryConfigurationSource source : sources) {
      i += count(source);
    }
    if (i == 0) {
      return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
    } else if (i == 1) {
      for (final RepositoryConfigurationSource source : sources) {
        if (source != null) {
          return source;
        }
      }
      throw new IllegalStateException();
    } else {
      final RepositoryConfigurationSource[] copy = new RepositoryConfigurationSource[i];
      i = 0;
      for (final RepositoryConfigurationSource source : sources) {
        i = copy(source, copy, i);
      }
      return new CombiningRepositoryConfigurationSource(copy);
    }
  }

  protected RepositoryConfigurationSource[] getSources() {
    return _sources;
  }

  @Override
  public RepositoryConfiguration getRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    for (final RepositoryConfigurationSource source : getSources()) {
      configs.addAll(source.getRepositoryConfiguration().getFunctions());
    }
    return new RepositoryConfiguration(configs);
  }

}
