/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.engine;

import java.util.concurrent.ExecutorService;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opengamma.core.security.Security;
import com.opengamma.sesame.credit.measures.CreditCs01Fn;
import com.opengamma.sesame.credit.measures.CreditPvFn;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.DefaultEngine;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.ViewFactory;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.solutions.library.tool.CreditPricer;

/**
 * Configures a {@link Engine} instance and associated objects.
 */
public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    // force componentMap and hence the threadLocal ServiceContext to be set now
    // (Some modules assume ServiceContext is set without declaring a dependency)
    bind(ComponentMap.class).toProvider(ComponentMapProvider.class).asEagerSingleton();
    bind(ViewFactory.class).toProvider(ViewFactoryProvider.class);
    bind(CreditPricer.class);
  }
  
  /**
   * Create the available outputs.
   * 
   * @return the available outputs, not null
   */
  @Provides
  public AvailableOutputs createAvailableOutputs() {
    AvailableOutputs available = new AvailableOutputsImpl(ImmutableSet.of(Security.class, TradeWrapper.class));
    available.register(CreditCs01Fn.class);
    available.register(CreditPvFn.class);
    return available;
  }

  /**
   * Create the engine instance
   *
   * @param viewFactory the view factory
   * @param marketData the MarketDataEnvironmentFactory
   * @return the engine
   */
  @Provides
  @Singleton
  public Engine createEngine(ViewFactory viewFactory, MarketDataEnvironmentFactory marketData) {
    return new DefaultEngine(viewFactory, marketData);
  }

  /**
   * Create the MarketDataEnvironmentFactory instance
   *
   * @param componentMap the ComponentMap
   * @return the MarketDataEnvironmentFactory
   */
  @Provides
  @Singleton
  public MarketDataEnvironmentFactory createEngine(ComponentMap componentMap) {
    return new MarketDataEnvironmentFactory(new EmptyMarketDataFactory(), MarketDataBuilders.raw(componentMap, "BLOOMBERG"));
  }

  /**
   * Create the ExecutorService instance,
   * a single thread executor is used here to aid clarity in debugging the examples
   *
   * @return the ExecutorService
   */
  @Provides
  @Singleton
  public ExecutorService createExecutorService() {
    return MoreExecutors.sameThreadExecutor();
  }


}
