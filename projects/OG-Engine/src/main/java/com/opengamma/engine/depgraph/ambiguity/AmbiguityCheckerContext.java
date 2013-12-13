/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * State for initializing an ambiguity checking service.
 */
public class AmbiguityCheckerContext {

  private final MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  private final FunctionCompilationContext _compilationContext;
  private final FunctionResolver _functionResolver;
  private final FunctionExclusionGroups _functionExclusionGroups;

  public AmbiguityCheckerContext(final MarketDataAvailabilityProvider marketDataAvailabilityProvider, final FunctionCompilationContext compilationContext, final FunctionResolver functionResolver,
      final FunctionExclusionGroups functionExclusionGroups) {
    _marketDataAvailabilityProvider = ArgumentChecker.notNull(marketDataAvailabilityProvider, "marketDataAvailabilityProvider");
    _compilationContext = ArgumentChecker.notNull(compilationContext, "compilationContext");
    _functionResolver = ArgumentChecker.notNull(functionResolver, "functionResolver");
    _functionExclusionGroups = functionExclusionGroups;
  }

  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _marketDataAvailabilityProvider;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _compilationContext;
  }

  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  public FunctionExclusionGroups getFunctionExclusionGroups() {
    return _functionExclusionGroups;
  }

}
