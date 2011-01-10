/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.calendar.Clock;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.calcnode.ViewProcessorQuery;
import com.opengamma.util.PublicAPI;

/**
 * Holds values that will be provided to a {@link FunctionInvoker} during invocation.
 */
@PublicAPI
public class FunctionExecutionContext extends AbstractFunctionContext {
  /**
   * The name under which an instance of {@link ViewProcessorQuery} should be bound.
   */
  public static final String VIEW_PROCESSOR_QUERY_NAME = "viewProcessorQuery";
  /**
   * The name under which the epoch time indicating the snapshot time will be bound.
   */
  public static final String SNAPSHOT_EPOCH_TIME_NAME = "snapshotEpochTime";
  /**
   * The name under which a JSR-310 Clock providing the snapshot time will be bound.
   */
  public static final String SNAPSHOT_CLOCK_NAME = "snapshotClock";
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
   * @param copyFrom context to copy elements from, not {@code null}
   */
  protected FunctionExecutionContext(final FunctionExecutionContext copyFrom) {
    super(copyFrom);
  }

  /**
   * Returns the {@link ViewProcessorQuery} bound to the context.
   * 
   * @return the query object
   */
  public ViewProcessorQuery getViewProcessorQuery() {
    return (ViewProcessorQuery) get(VIEW_PROCESSOR_QUERY_NAME);
  }

  /**
   * Binds a {@link ViewProcessorQuery} instance to the context
   * 
   * @param viewProcessorQuery the query object
   */
  public void setViewProcessorQuery(ViewProcessorQuery viewProcessorQuery) {
    put(VIEW_PROCESSOR_QUERY_NAME, viewProcessorQuery);
  }

  /**
   * Returns the snapshot time for the context.
   * 
   * @return the snapshot time
   */
  public Long getSnapshotEpochTime() {
    return (Long) get(SNAPSHOT_EPOCH_TIME_NAME);
  }

  /**
   * Sets the snapshot time for the context.
   * 
   * @param snapshotEpochTime the snapshot time
   */
  public void setSnapshotEpochTime(Long snapshotEpochTime) {
    put(SNAPSHOT_EPOCH_TIME_NAME, snapshotEpochTime);
  }

  /**
   * Returns the clock providing the snapshot time.
   * 
   * @return the clock instance
   */
  public Clock getSnapshotClock() {
    return (Clock) get(SNAPSHOT_CLOCK_NAME);
  }

  /**
   * Sets the clock providing the snapshot time.
   * 
   * @param snapshotClock the clock instance
   */
  public void setSnapshotClock(Clock snapshotClock) {
    put(SNAPSHOT_CLOCK_NAME, snapshotClock);
  }

  /**
   * Binds a {@link SecuritySource} instance to the context.
   * 
   * @param securitySource the instance to bind
   */
  public void setSecuritySource(SecuritySource securitySource) {
    put(SECURITY_SOURCE_NAME, securitySource);
  }

  /**
   * Returns the {@link SecuritySource} bound to the context.
   * 
   * @return the security source
   */
  public SecuritySource getSecuritySource() {
    return (SecuritySource) get(SECURITY_SOURCE_NAME);
  }

  /**
   * Binds a {@link FunctionParameters} instance to the context.
   * 
   * @param functionParameters the instance to bind
   */
  public void setFunctionParameters(FunctionParameters functionParameters) {
    put(FUNCTION_PARAMETERS_NAME, functionParameters);
  }

  /**
   * Returns the {@link FunctionParameters} bound to the context.
   * 
   * @return the function parameters
   */
  public FunctionParameters getFunctionParameters() {
    return (FunctionParameters) get(FUNCTION_PARAMETERS_NAME);
  }

  /**
   * Binds a {@link PortfolioStructure} instance to the context.
   * 
   * @param portfolioStructure the instance to bind
   */
  public void setPortfolioStructure(final PortfolioStructure portfolioStructure) {
    put(PORTFOLIO_STRUCTURE_NAME, portfolioStructure);
  }

  /**
   * Returns the {@link PortfolioStructure} bound to the context
   * 
   * @return the portfolio structure query object
   */
  public PortfolioStructure getPortfolioStructure() {
    return (PortfolioStructure) get(PORTFOLIO_STRUCTURE_NAME);
  }

  @Override
  public FunctionExecutionContext clone() {
    return new FunctionExecutionContext(this);
  }

}
