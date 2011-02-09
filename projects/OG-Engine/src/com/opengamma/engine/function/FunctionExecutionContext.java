/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

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
   * Gets the snapshot instant.
   * 
   * @return the snapshot instant, null if not in the context
   */
  public Long getSnapshotEpochTime() {
    return (Long) get(SNAPSHOT_EPOCH_TIME_NAME);
  }

  /**
   * Sets the snapshot instant.
   * 
   * @param snapshotEpochTime  the snapshot instant to bind
   */
  public void setSnapshotEpochTime(Long snapshotEpochTime) {
    put(SNAPSHOT_EPOCH_TIME_NAME, snapshotEpochTime);
  }

  /**
   * Gets the clock providing the snapshot time.
   * 
   * @return the clock, null if not in the context
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

  //-------------------------------------------------------------------------
  @Override
  public FunctionExecutionContext clone() {
    return new FunctionExecutionContext(this);
  }

}
