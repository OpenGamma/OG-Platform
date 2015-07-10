/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.change.PassthroughChangeManager;

/**
 * Combines the function configuration from two or more sources into a single configuration object.
 */
public class CombiningFunctionConfigurationSource implements FunctionConfigurationSource {

  private final FunctionConfigurationSource[] _sources;

  protected CombiningFunctionConfigurationSource(final FunctionConfigurationSource[] sources) {
    _sources = sources;
  }

  private static int count(final FunctionConfigurationSource source) {
    if (source == null) {
      return 0;
    } else if (source instanceof CombiningFunctionConfigurationSource) {
      int count = 0;
      for (final FunctionConfigurationSource inner : ((CombiningFunctionConfigurationSource) source).getSources()) {
        count += count(inner);
      }
      return count;
    } else {
      return 1;
    }
  }

  private static int copy(final FunctionConfigurationSource source, final FunctionConfigurationSource[] dest, int index) {
    if (source != null) {
      if (source instanceof CombiningFunctionConfigurationSource) {
        for (final FunctionConfigurationSource inner : ((CombiningFunctionConfigurationSource) source).getSources()) {
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
  public static FunctionConfigurationSource of(final FunctionConfigurationSource... sources) {
    if ((sources == null) || (sources.length == 0)) {
      return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
    }
    int i = 0;
    for (final FunctionConfigurationSource source : sources) {
      i += count(source);
    }
    if (i == 0) {
      return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
    } else if (i == 1) {
      for (final FunctionConfigurationSource source : sources) {
        if (source != null) {
          return source;
        }
      }
      throw new IllegalStateException();
    } else {
      final FunctionConfigurationSource[] copy = new FunctionConfigurationSource[i];
      i = 0;
      for (final FunctionConfigurationSource source : sources) {
        i = copy(source, copy, i);
      }
      return new CombiningFunctionConfigurationSource(copy);
    }
  }

  protected FunctionConfigurationSource[] getSources() {
    return _sources;
  }

  @Override
  public FunctionConfigurationBundle getFunctionConfiguration(final Instant version) {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    for (final FunctionConfigurationSource source : getSources()) {
      configs.addAll(source.getFunctionConfiguration(version).getFunctions());
    }
    return new FunctionConfigurationBundle(configs);
  }

  @Override
  public ChangeManager changeManager() {
    PassthroughChangeManager changeManager = null;
    for (FunctionConfigurationSource source : getSources()) {
      final ChangeManager cm = source.changeManager();
      if (cm != DummyChangeManager.INSTANCE) {
        if (changeManager == null) {
          changeManager = new PassthroughChangeManager();
        }
        changeManager.addChangeManager(cm);
      }
    }
    if (changeManager == null) {
      return DummyChangeManager.INSTANCE;
    } else {
      return changeManager;
    }
  }

}
