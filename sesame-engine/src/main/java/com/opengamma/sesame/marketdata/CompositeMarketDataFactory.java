/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory that creates {@link MarketDataSource} instances that use multiple underlying data sources.
 */
public class CompositeMarketDataFactory implements MarketDataFactory<MarketDataSpecification> {

  private final Map<Class<? extends MarketDataSpecification>, MarketDataFactory<?>> _factories;

  /**
   * Creates a factory that combines data from multiple other factories.
   *
   * @param factories  the factories that are the source of the data
   */
  public CompositeMarketDataFactory(MarketDataFactory<?>... factories) {
    this(ImmutableList.copyOf(factories));
  }

  /**
   * Creates a factory that combines data from multiple other factories.
   *
   * @param factories  the factories that are the source of the data
   */
  public CompositeMarketDataFactory(List<MarketDataFactory<?>> factories) {
    Map<Class<? extends MarketDataSpecification>, MarketDataFactory<?>> factoryMap = new HashMap<>();

    for (MarketDataFactory<?> factory : factories) {
      factoryMap.put(factory.getSpecificationType(), factory);
    }
    if (!factoryMap.containsKey(EmptyMarketDataSpec.class)) {
      factoryMap.put(EmptyMarketDataSpec.class, new EmptyMarketDataFactory());
    }
    _factories = ImmutableMap.copyOf(factoryMap);
  }

  @Override
  public Class<MarketDataSpecification> getSpecificationType() {
    return MarketDataSpecification.class;
  }

  @Override
  public MarketDataSource create(MarketDataSpecification spec) {
    if (spec instanceof CompositeMarketDataSpecification) {
      return create(((CompositeMarketDataSpecification) spec).getSpecifications());
    } else {
      return getFactory(spec).create(spec);
    }
  }

  private MarketDataSource create(List<MarketDataSpecification> specs) {
    ArgumentChecker.notNull(specs, "specs");
    List<MarketDataSource> dataSources = new ArrayList<>(specs.size());

    for (MarketDataSpecification spec : specs) {
      dataSources.add(getFactory(spec).create(spec));
    }
    return new CompositeMarketDataSource(dataSources);
  }

  private MarketDataFactory<MarketDataSpecification> getFactory(MarketDataSpecification spec) {
    @SuppressWarnings("unchecked")
    MarketDataFactory<MarketDataSpecification> factory =
        (MarketDataFactory<MarketDataSpecification>) _factories.get(spec.getClass());

    if (factory == null) {
      throw new IllegalArgumentException("Unknown market data specification type, spec = " + spec);
    }
    return factory;
  }
}
