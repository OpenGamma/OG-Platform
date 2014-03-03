/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
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
   * The name under which the target resolver will be bound.
   */
  public static final String COMPUTATION_TARGET_RESOLVER_NAME = "targetResolver";
  /**
   * Resolver for mapping the resolved computation targets to the preferred external identifiers.
   */
  public static final String EXTERNAL_IDENTIFIER_LOOKUP = "externalIdLookup";
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
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String ORGANIZATION_SOURCE_NAME = "legalEntitySource";
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

  public ComputationTargetResolver.AtVersionCorrection getComputationTargetResolver() {
    return (ComputationTargetResolver.AtVersionCorrection) get(COMPUTATION_TARGET_RESOLVER_NAME);
  }

  public void setComputationTargetResolver(final ComputationTargetResolver.AtVersionCorrection targetResolver) {
    put(COMPUTATION_TARGET_RESOLVER_NAME, targetResolver);
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
  public void setValuationTime(final Instant valuationTime) {
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
  public void setValuationClock(final Clock snapshotClock) {
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
   * @param legalEntitySource  the source of organizations to bind
   */
  public void setLegalEntitySource(final LegalEntitySource legalEntitySource) {
    put(ORGANIZATION_SOURCE_NAME, legalEntitySource);
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
  public void setFunctionParameters(final FunctionParameters functionParameters) {
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
  public <T extends SecuritySource> T getSecuritySource(final Class<T> clazz) {
    return clazz.cast(get(SECURITY_SOURCE_NAME));
  }

  @Override
  public FunctionExecutionContext clone() {
    return new FunctionExecutionContext(this);
  }

}
