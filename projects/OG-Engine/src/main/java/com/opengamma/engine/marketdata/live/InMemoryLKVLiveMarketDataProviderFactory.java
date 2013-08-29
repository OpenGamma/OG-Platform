/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for {@link MarketDataProvider}s backed by live data.
 */
public class InMemoryLKVLiveMarketDataProviderFactory implements LiveMarketDataProviderFactory {

  private final LiveDataFactory _defaultFactory;
  private final Map<String, LiveDataFactory> _namedFactories;
  private final List<String> _providerNames;

  public InMemoryLKVLiveMarketDataProviderFactory(LiveDataFactory defaultFactory, Map<String, LiveDataFactory> namedFactories) {
    ArgumentChecker.notNull(defaultFactory, "defaultFactory");
    ArgumentChecker.notNull(namedFactories, "namedFactories");
    _defaultFactory = defaultFactory;
    _namedFactories = ImmutableMap.copyOf(namedFactories);
    
    String defaultFactoryName = null;
    for (Map.Entry<String, LiveDataFactory> namedFactoryEntry : namedFactories.entrySet()) {
      LiveDataFactory factory = namedFactoryEntry.getValue();
      if (defaultFactory == factory) {
        defaultFactoryName = namedFactoryEntry.getKey();
        continue;
      }
    }
    if (defaultFactoryName == null) {
      throw new OpenGammaRuntimeException("Default factory not found in named factories map");
    }

    List<String> providerNames = new ArrayList<>(_namedFactories.keySet());
    Collections.sort(providerNames);
    providerNames.remove(defaultFactoryName);
    providerNames.add(0, defaultFactoryName);
    _providerNames = ImmutableList.copyOf(providerNames);
  }

  @Override
  public MarketDataProvider create(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(marketDataSpec, "marketDataSpec");
    LiveMarketDataSpecification liveSpec = (LiveMarketDataSpecification) marketDataSpec;
    if (liveSpec.getDataSource() == null) {
      return _defaultFactory.create(user);
    }
    LiveDataFactory liveDataFactory = _namedFactories.get(liveSpec.getDataSource());
    if (liveDataFactory == null) {
      throw new IllegalArgumentException("No provider could be created for data source '" + liveSpec.getDataSource() + "'");
    }
    return liveDataFactory.create(user);
  }

  @Override
  public List<String> getProviderNames() {
    return _providerNames;
  }
}
