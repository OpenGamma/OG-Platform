/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Context for use with the REST diagnostic dependency graph builder.
 */
public class DependencyGraphBuilderResourceContextBean implements InitializingBean {

  private DependencyGraphBuilderFactory _graphBuilders = new DependencyGraphBuilderFactory();
  private MarketDataProviderResolver _marketDataProviderResolver;
  private FunctionCompilationContext _compilationContext;
  private FunctionResolver _functionResolver;
  private FunctionExclusionGroups _functionExclusionGroups;

  public void setDependencyGraphBuilderFactory(final DependencyGraphBuilderFactory graphBuilders) {
    _graphBuilders = graphBuilders;
  }

  public DependencyGraphBuilderFactory getDependencyGraphBuilderFactory() {
    return _graphBuilders;
  }

  public void setMarketDataProviderResolver(final MarketDataProviderResolver marketDataProviderResolver) {
    _marketDataProviderResolver = marketDataProviderResolver;
  }

  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  public void setFunctionCompilationContext(final FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _compilationContext;
  }

  public void setFunctionResolver(final FunctionResolver functionResolver) {
    _functionResolver = functionResolver;
  }

  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  public void setFunctionExclusionGroups(final FunctionExclusionGroups functionExclusionGroups) {
    _functionExclusionGroups = functionExclusionGroups;
  }

  public FunctionExclusionGroups getFunctionExclusionGroups() {
    return _functionExclusionGroups;
  }

  public void setCompiledFunctionService(final CompiledFunctionService cfs) {
    setFunctionCompilationContext(cfs.getFunctionCompilationContext());
    if (getFunctionResolver() == null) {
      setFunctionResolver(new DefaultFunctionResolver(cfs));
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getDependencyGraphBuilderFactory(), "dependencyGraphBuilderFactory");
    ArgumentChecker.notNull(getMarketDataProviderResolver(), "marketDataProviderResolver");
    ArgumentChecker.notNull(getFunctionCompilationContext(), "functionCompilationContext");
    ArgumentChecker.notNull(getFunctionResolver(), "functionResolver");
  }

}
