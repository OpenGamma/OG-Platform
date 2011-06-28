/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;

/**
 * Resolves a {@link MarketDataSnapshotSpecification} into a {@link MarketDataProvider} by specification type.
 */
public class TypeBasedMarketDataProviderResolver implements MarketDataProviderResolver {

  private final Map<Class<?>, MarketDataProvider> _providerMap = new HashMap<Class<?>, MarketDataProvider>();
  
  public <S extends MarketDataSnapshotSpecification> void addProvider(Class<S> specificationType, MarketDataProvider provider) {
    _providerMap.put(specificationType, provider);
  }
  
  @Override
  public MarketDataProvider resolve(MarketDataSnapshotSpecification snapshotSpec) {
    return _providerMap.get(snapshotSpec.getClass());
  }

}
