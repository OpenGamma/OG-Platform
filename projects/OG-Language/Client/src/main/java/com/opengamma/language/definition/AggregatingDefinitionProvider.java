/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * A {@link DefinitionProvider} implementation that aggregates a number of other providers. If multiple providers supply a
 * definition with the same name, the definition from the later provider in the aggregation list will be used.  
 * 
 * @param <T> the definition type
 */
public abstract class AggregatingDefinitionProvider<T extends Definition> extends AbstractDefinitionProvider<T> {

  private final List<DefinitionProvider<T>> _providers = new ArrayList<DefinitionProvider<T>>();

  protected AggregatingDefinitionProvider(final boolean enableCache) {
    super(enableCache);
  }

  protected List<DefinitionProvider<T>> getProviders() {
    return _providers;
  }

  public void addProvider(final DefinitionProvider<T> provider) {
    ArgumentChecker.notNull(provider, "provider");
    getProviders().add(provider);
  }

  // AbstractFunctionProvider

  @Override
  public final void flush() {
    super.flush();
    for (DefinitionProvider<T> provider : getProviders()) {
      provider.flush();
    }
  }

  @Override
  protected final void loadDefinitions(final Collection<T> definitions) {
    final Map<String, T> definitionMap = new HashMap<String, T>();
    for (DefinitionProvider<T> provider : getProviders()) {
      final Set<T> providerDefinitions = provider.getDefinitions();
      if (providerDefinitions != null) {
        for (T definition : providerDefinitions) {
          definitionMap.put(definition.getName(), definition);
        }
      }
    }
    definitions.addAll(definitionMap.values());
  }

}
