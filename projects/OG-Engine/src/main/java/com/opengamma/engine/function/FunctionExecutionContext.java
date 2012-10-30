/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.Instant;
import javax.time.calendar.Clock;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.calcnode.ViewProcessorQuery;
import com.opengamma.util.PublicAPI;

/**
 * The context used during function invocation.
 * <p>
 * In order to successfully complete invocation of a function a variety of
 * contextual objects are needed.
 * This is primarily used by {@link FunctionInvoker}.
 */
@PublicAPI
public class FunctionExecutionContext extends AbstractFunctionContext {

  /**
   * The name under which an instance of {@link ViewProcessorQuery} should be bound.
   */
  public static final String VIEW_PROCESSOR_QUERY_NAME = "viewProcessorQuery";
  /**
   * The name under which the valuation instant will be bound.
   */
  public static final String VALUATION_INSTANT_NAME = "valuationInstant";
  /**
   * The name under which a Clock providing the valuation time will be bound.
   */
  public static final String VALUATION_CLOCK_NAME = "valuationClock";
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";
  /**
   * The name under which function parameters (such as # of Monte Carlo iterations) should be bound.
   */
  public static final String FUNCTION_PARAMETERS_NAME = "functionParameters";
  /**
   * The name under which an instance of {@link PortfolioStructure} should be bound.
   */
  public static final String PORTFOLIO_STRUCTURE_NAME = "portfolioStructure";

  /**
   * Creates an empty function execution context.
   */
  public FunctionExecutionContext() {
  }

  /**
   * Creates a function execution context as a copy of another.
   * 
   * @param copyFrom  the context to copy elements from, not null
   */
  protected FunctionExecutionContext(final FunctionExecutionContext copyFrom) {
    super(copyFrom);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the view processor query.
   * 
   * @return the view processor query, null if not in the context
   */
  public ViewProcessorQuery getViewProcessorQuery() {
    return (ViewProcessorQuery) get(VIEW_PROCESSOR_QUERY_NAME);
  }

  /**
   * Sets the view processor query.
   * 
   * @param viewProcessorQuery  the view processor query to bind
   */
  public void setViewProcessorQuery(ViewProcessorQuery viewProcessorQuery) {
    put(VIEW_PROCESSOR_QUERY_NAME, viewProcessorQuery);
  }

  /**
   * Gets the valuation time.
   * 
   * @return the valuation time, null if not in the context
   */
  public Instant getValuationTime() {
    return (Instant) get(VALUATION_INSTANT_NAME);
  }

  /**
   * Sets the valuation time.
   * 
   * @param valuationTime the valuation time to bind
   */
  public void setValuationTime(Instant valuationTime) {
    put(VALUATION_INSTANT_NAME, valuationTime);
  }

  /**
   * Gets the clock providing the valuation time.
   * 
   * @return the clock, null if not in the context
   */
  public Clock getValuationClock() {
    return (Clock) get(VALUATION_CLOCK_NAME);
  }

  /**
   * Sets the clock providing the valuation time.
   * 
   * @param snapshotClock the clock instance
   */
  public void setValuationClock(Clock snapshotClock) {
    put(VALUATION_CLOCK_NAME, snapshotClock);
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
   * Gets the function parameters.
   * 
   * @return the function parameters, null if not in the context
   */
  public FunctionParameters getFunctionParameters() {
    return (FunctionParameters) get(FUNCTION_PARAMETERS_NAME);
  }

  /**
   * Sets the source of function parameters.
   * 
   * @param functionParameters  the function parameters to bind
   */
  public void setFunctionParameters(FunctionParameters functionParameters) {
    put(FUNCTION_PARAMETERS_NAME, functionParameters);
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

  @Override
  public FunctionExecutionContext clone() {
    return new FunctionExecutionContext(this);
  }

}
