/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;

/**
 * Resolver for {@link MarketDataProvider}s 
 */
public interface MarketDataProviderResolver {

  /**
   * Resolves a {@link MarketDataSnapshotSpecification} into a {@link MarketDataProvider} which is able to 
   * satisfy the specification.
   * 
   * @param snapshotSpec  the snapshot specification for which a provider is required, not {@code null} 
   * @return the resolved snapshot provider
   */
  MarketDataProvider resolve(MarketDataSnapshotSpecification snapshotSpec);
  
}
