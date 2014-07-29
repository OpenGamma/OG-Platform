/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import org.threeten.bp.Instant;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicAPI;

/**
 * The context used during expression compilation.
 * <p>
 * In order to successfully complete expression compilation a variety of contextual objects are needed. This is primarily used by {@link FunctionDefinition}.
 */
@PublicAPI
public class FunctionCompilationContext extends AbstractFunctionContext {

  /**
   * The name under which the {@link ComputationTargetResolver.AtVersionCorrection} instance should be bound.
   */
  public static final String COMPUTATION_TARGET_RESOLVER = "computationTargetResolver";
  /**
   * The name under which the {@link ComputationTargetResults} instance should be bound.
   */
  public static final String COMPUTATION_TARGET_RESULTS_NAME = "computationTargetResults";
  /**
   * The name under which the initialization reference of the functions should be bound.
   */
  public static final String FUNCTION_INIT_ID_NAME = "functionInitialization";
  /**
   * The name under which the initialization timestamp should be bound.
   */
  public static final String FUNCTION_INIT_TIMESTAMP_NAME = "functionInitializationTimestamp";
  /**
   * The name under which a re-initialization hook should be bound.
   */
  public static final String FUNCTION_REINITIALIZER_NAME = "functionReinitializer";
  /**
   * The name under which the graph building blacklist should be bound.
   */
  public static final String GRAPH_BUILDING_BLACKLIST = "graphBuildingBlacklist"; // TODO: [PLAT-2638] The blacklists should not really be here.
  /**
   * The name under which the graph execution blacklist should be bound.
   */
  public static final String GRAPH_EXECUTION_BLACKLIST = "graphExecutionBlacklist"; // TODO: [PLAT-2638] The blacklists should not really be here.
  /**
   * The name under which the portfolio should be point.
   */
  public static final String PORTFOLIO = "portfolio";
  /**
   * The name under which an instance of {@link PortfolioStructure} should be bound.
   */
  public static final String PORTFOLIO_STRUCTURE_NAME = "portfolioStructure";
  /**
   * The name under which the {@link ComputationTargetResolver} instance should be bound.
   */
  public static final String RAW_COMPUTATION_TARGET_RESOLVER = "rawComputationTargetResolver";
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";
  /**
   * The name under which an instance of {@link LegalEntitySource} should be bound.
   */
  public static final String ORGANIZATION_SOURCE_NAME = "legalEntitySource";
  /**
   * The name under which the view calculation configuration should be bound.
   */
  public static final String VIEW_CALCULATION_CONFIGURATION_NAME = "viewCalculationConfiguration";

  /**
   * Creates an empty function compilation context.
   */
  public FunctionCompilationContext() {
    setGraphBuildingBlacklist(new DummyFunctionBlacklistQuery());
    setGraphExecutionBlacklist(new DummyFunctionBlacklistQuery());
  }

  /**
   * Creates a function compilation context as a copy of another.
   * 
   * @param copyFrom the context to copy elements from, not null
   */
  protected FunctionCompilationContext(final FunctionCompilationContext copyFrom) {
    super(copyFrom);
  }

  /**
   * Gets the computation target resolver configured for the correct version/correction. Functions shouldn't need to access this directly as their target will always be resolved when they are invoked.
   * 
   * @return the computation target resolver, null if not in the context
   */
  public ComputationTargetResolver.AtVersionCorrection getComputationTargetResolver() {
    return (ComputationTargetResolver.AtVersionCorrection) get(COMPUTATION_TARGET_RESOLVER);
  }

  /**
   * Sets the computation target resolver.
   * 
   * @param computationTargetResolver the target resolver
   */
  public void setComputationTargetResolver(final ComputationTargetResolver.AtVersionCorrection computationTargetResolver) {
    put(COMPUTATION_TARGET_RESOLVER, computationTargetResolver);
  }

  /**
   * Gets the raw computation target resolver. Functions should not need to access this directly - the resolver returned by {@link #getComputationTargetResolver} is correctly configured for the
   * version/correction time the owning view process needs.
   * 
   * @return the computation target resolver, null if not in the context
   */
  public ComputationTargetResolver getRawComputationTargetResolver() {
    return (ComputationTargetResolver) get(RAW_COMPUTATION_TARGET_RESOLVER);
  }

  /**
   * Sets the computation target resolver.
   * 
   * @param computationTargetResolver the target resolver
   */
  public void setRawComputationTargetResolver(final ComputationTargetResolver computationTargetResolver) {
    put(RAW_COMPUTATION_TARGET_RESOLVER, computationTargetResolver);
  }

  /**
   * Gets the source of result information on a target.
   * 
   * @return the source of target results, null if none is available
   */
  public ComputationTargetResults getComputationTargetResults() {
    return (ComputationTargetResults) get(COMPUTATION_TARGET_RESULTS_NAME);
  }

  /**
   * Sets the source of result information on a target.
   * 
   * @param computationTargetResults the source of target results, null to remove it from the context
   */
  public void setComputationTargetResults(final ComputationTargetResults computationTargetResults) {
    if (computationTargetResults == null) {
      remove(COMPUTATION_TARGET_RESULTS_NAME);
    } else {
      put(COMPUTATION_TARGET_RESULTS_NAME, computationTargetResults);
    }
  }

  /**
   * Gets the source of securities.
   * 
   * @return the source of securities, null if not in the context
   */
  public SecuritySource getSecuritySource() {
    return (SecuritySource) get(SECURITY_SOURCE_NAME);
  }

  /**
   * Sets the source of securities.
   * 
   * @param securitySource the source of securities to bind
   */
  public void setSecuritySource(final SecuritySource securitySource) {
    put(SECURITY_SOURCE_NAME, securitySource);
  }

  /**
   * Gets the source of organizations.
   * 
   * @return the source of organizations, null if not in the context
   */
  public LegalEntitySource getLegalEntitySource() {
    return (LegalEntitySource) get(ORGANIZATION_SOURCE_NAME);
  }

  /**
   * Sets the source of organizations.
   *
   * @param legalEntitySource the source of organizations to bind
   */
  public void setLegalEntitySource(final LegalEntitySource legalEntitySource) {
    put(ORGANIZATION_SOURCE_NAME, legalEntitySource);
  }

  /**
   * Gets the portfolio being compiled, if any.
   * 
   * @return the portfolio object, or null if there is none for the current compilation
   */
  public Portfolio getPortfolio() {
    return (Portfolio) get(PORTFOLIO);
  }

  /**
   * Sets the portfolio being compiled, if any.
   * 
   * @param portfolio the portfolio object, or null if there is none for the current compilation
   */
  public void setPortfolio(final Portfolio portfolio) {
    put(PORTFOLIO, portfolio);
  }

  /**
   * Gets the source of portfolio structure information.
   * 
   * @return the portfolio structure, null if not in the context
   */
  public PortfolioStructure getPortfolioStructure() {
    return (PortfolioStructure) get(PORTFOLIO_STRUCTURE_NAME);
  }

  /**
   * Sets the source of portfolio structure information.
   * 
   * @param portfolioStructure the portfolio structure to bind
   */
  public void setPortfolioStructure(final PortfolioStructure portfolioStructure) {
    put(PORTFOLIO_STRUCTURE_NAME, portfolioStructure);
  }

  /**
   * Gets the view calculation configuration information. This may only be valid during dependency graph construction and not during function initialization or compilation.
   * 
   * @return the view configuration, null if not in the context
   */
  public ViewCalculationConfiguration getViewCalculationConfiguration() {
    return (ViewCalculationConfiguration) get(VIEW_CALCULATION_CONFIGURATION_NAME);
  }

  /**
   * Sets the view calculation configuration information. This should be set prior to dependency graph construction.
   * 
   * @param viewCalculationConfiguration the configuration to bind
   */
  public void setViewCalculationConfiguration(final ViewCalculationConfiguration viewCalculationConfiguration) {
    put(VIEW_CALCULATION_CONFIGURATION_NAME, viewCalculationConfiguration);
  }

  /**
   * Gets the function initialization identifier.
   * 
   * @return the identifier, null if not in the context
   */
  public Long getFunctionInitId() {
    return (Long) get(FUNCTION_INIT_ID_NAME);
  }

  /**
   * Sets the function initialization identifier.
   * 
   * @param id the identifier to bind
   */
  public void setFunctionInitId(final long id) {
    put(FUNCTION_INIT_ID_NAME, id);
    // TODO: Note that the behaviour below is closely coupled to the implementation of initialization identifiers in CompiledFunctionService
    final Instant instant = Instant.ofEpochMilli(id);
    put(FUNCTION_INIT_TIMESTAMP_NAME, VersionCorrection.of(instant, instant));
  }

  /**
   * Gets the function re-initialization hook.
   * 
   * @return the re-initialization hook, null if not in the context
   */
  public FunctionReinitializer getFunctionReinitializer() {
    return (FunctionReinitializer) get(FUNCTION_REINITIALIZER_NAME);
  }

  /**
   * Sets the function re-initialization hook.
   * 
   * @param reinitializer the re-initialization hook to bind
   */
  public void setFunctionReinitializer(final FunctionReinitializer reinitializer) {
    if (reinitializer == null) {
      remove(FUNCTION_REINITIALIZER_NAME);
    } else {
      put(FUNCTION_REINITIALIZER_NAME, reinitializer);
    }
  }

  /**
   * Returns the version/correction timestamp that should be used to obtain initialization data.
   * <p>
   * There is no explicit setter for this context member as the current implementation of {@link CompiledFunctionService} uses the initialization identifier in a particular fashion. Functions should
   * not rely on this relationship as it may be removed/changed in future releases.
   * 
   * @return the initialization timestamp - functions should use this to query their initialization configuration instead of {@link VersionCorrection#LATEST}.
   */
  public VersionCorrection getFunctionInitializationVersionCorrection() {
    return (VersionCorrection) get(FUNCTION_INIT_TIMESTAMP_NAME);
  }

  /**
   * Returns the function blacklist to be used during graph building. Graphs should not be built containing any items that are currently on the blacklist.
   * 
   * @return the query interface to the blacklist, not null
   */
  public FunctionBlacklistQuery getGraphBuildingBlacklist() {
    return (FunctionBlacklistQuery) get(GRAPH_BUILDING_BLACKLIST);
  }

  /**
   * Sets the function blacklist to be used during graph building. Graphs should not be built containing any items that are currently on the blacklist.
   * 
   * @param graphBuildingBlacklist interface to the blacklist to use, not null
   */
  public void setGraphBuildingBlacklist(final FunctionBlacklistQuery graphBuildingBlacklist) {
    put(GRAPH_BUILDING_BLACKLIST, graphBuildingBlacklist);
  }

  /**
   * Returns the function blacklist to use when executing a graph. This is part of the compilation context because the blacklist applies immediately before the graph is submitted for execution, before
   * an execution context is valid.
   * 
   * @return the execution blacklist, not null
   */
  public FunctionBlacklistQuery getGraphExecutionBlacklist() {
    return (FunctionBlacklistQuery) get(GRAPH_EXECUTION_BLACKLIST);
  }

  /**
   * Sets the function blacklist to use when executing a graph. This is part of the compilation context because the blacklist applies immediately before the graph is submitted for execution, before an
   * execution context is valid.
   * 
   * @param blacklist the execution blacklist, not null
   */
  public void setGraphExecutionBlacklist(final FunctionBlacklistQuery blacklist) {
    put(GRAPH_EXECUTION_BLACKLIST, blacklist);
  }

  /**
   * Gets the source of securities cast to a specific type.
   * 
   * @param <T> the security source type
   * @param clazz the security source type
   * @return the security source
   * @throws ClassCastException if the security source is of a different type
   */
  public <T extends SecuritySource> T getSecuritySource(final Class<T> clazz) {
    return clazz.cast(get(SECURITY_SOURCE_NAME));
  }

  /**
   * Initialises the context. Any members that implement {@link FunctionCompilationContextAware} will receive appropriate callbacks.
   */
  public void init() {
    for (final Object member : getAllElements()) {
      if (member instanceof FunctionCompilationContextAware) {
        ((FunctionCompilationContextAware) member).setFunctionCompilationContext(this);
      }
    }
  }

  /**
   * Produces a copy of the context.
   * 
   * @return the copy
   */
  @Override
  public FunctionCompilationContext clone() {
    return new FunctionCompilationContext(this);
  }

}
