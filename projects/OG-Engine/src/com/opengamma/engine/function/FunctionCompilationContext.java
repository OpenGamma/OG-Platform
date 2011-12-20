/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.util.PublicAPI;

/**
 * The context used during expression compilation.
 * <p>
 * In order to successfully complete expression compilation a variety of
 * contextual objects are needed.
 * This is primarily used by {@link FunctionDefinition}.
 */
@PublicAPI
public class FunctionCompilationContext extends AbstractFunctionContext {

  /**
   * The name under which the {@link ComputationTargetResults} instance should be bound.
   */
  public static final String COMPUTATION_TARGET_RESULTS_NAME = "computationTargetResults";
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";
  /**
   * The name under which an instance of {@link PortfolioStructure} should be bound.
   */
  public static final String PORTFOLIO_STRUCTURE_NAME = "portfolioStructure";
  /**
   * The name under which the view calculation configuration should be bound.
   */
  public static final String VIEW_CALCULATION_CONFIGURATION_NAME = "viewCalculationConfiguration";
  /**
   * The name under which the initialization reference of the functions should be bound.
   */
  public static final String FUNCTION_INIT_ID_NAME = "functionInitialization";
  /**
   * The name under which a re-initialization hook should be bound.
   */
  public static final String FUNCTION_REINITIALIZER_NAME = "functionReinitializer";

  /**
   * Creates an empty function compilation context.
   */
  public FunctionCompilationContext() {
  }

  /**
   * Creates a function compilation context as a copy of another.
   * 
   * @param copyFrom  the context to copy elements from, not null
   */
  protected FunctionCompilationContext(final FunctionCompilationContext copyFrom) {
    super(copyFrom);
  }

  //-------------------------------------------------------------------------
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
   * @param computationTargetResults the source of target results
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
   * @param securitySource  the source of securities to bind
   */
  public void setSecuritySource(SecuritySource securitySource) {
    put(SECURITY_SOURCE_NAME, securitySource);
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
   * @param portfolioStructure  the portfolio structure to bind
   */
  public void setPortfolioStructure(final PortfolioStructure portfolioStructure) {
    put(PORTFOLIO_STRUCTURE_NAME, portfolioStructure);
  }

  /**
   * Gets the view calculation configuration information.
   * This may only be valid during dependency graph construction and not during
   * function initialization or compilation.
   * 
   * @return the view configuration, null if not in the context
   */
  public ViewCalculationConfiguration getViewCalculationConfiguration() {
    return (ViewCalculationConfiguration) get(VIEW_CALCULATION_CONFIGURATION_NAME);
  }

  /**
   * Sets the view calculation configuration information. This should be set prior to dependency graph construction.
   * @param viewCalculationConfiguration  the configuration to bind
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
   * @param id  the identifier to bind
   */
  public void setFunctionInitId(final long id) {
    put(FUNCTION_INIT_ID_NAME, id);
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
   * @param reinitializer  the re-initialization hook to bind
   */
  public void setFunctionReinitializer(final FunctionReinitializer reinitializer) {
    if (reinitializer == null) {
      remove(FUNCTION_REINITIALIZER_NAME);
    } else {
      put(FUNCTION_REINITIALIZER_NAME, reinitializer);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source of securities cast to a specific type.
   * 
   * @param <T>  the security source type
   * @param clazz  the security source type
   * @return the security source
   * @throws ClassCastException if the security source is of a different type
   */
  public <T extends SecuritySource> T getSecuritySource(Class<T> clazz) {
    return clazz.cast(get(SECURITY_SOURCE_NAME));
  }

  /**
   * Produces a copy of the context.
   * @return the copy
   */
  @Override
  public FunctionCompilationContext clone() {
    return new FunctionCompilationContext(this);
  }

}
