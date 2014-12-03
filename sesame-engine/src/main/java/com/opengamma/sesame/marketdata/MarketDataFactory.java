/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Factory for {@link MarketDataSource} instances.
 * <p>
 * A factory implementation creates data source instances when an instance of {@link MarketDataSpecification}
 * is passed to {@link #create(MarketDataSpecification)}. Each factory declares what specification types
 * it can handle with {@link #getSpecificationType()} and the type parameter.
 *
 * @param <T> the type of {@link MarketDataSpecification} this factory can handle
 */
public interface MarketDataFactory<T extends MarketDataSpecification> {

  /**
   * Returns the type of the {@link MarketDataSpecification} handled by this factory.
   *
   * @return the type of the {@link MarketDataSpecification} handled by this factory
   */
  Class<T> getSpecificationType();

  /**
   * Creates a {@link MarketDataSource} for a given specification.
   * 
   * @param spec  the market data specification, not null
   * @return the market data source, not null
   */
  MarketDataSource create(T spec);
  
}
