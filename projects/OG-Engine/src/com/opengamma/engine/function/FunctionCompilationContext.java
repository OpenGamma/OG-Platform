/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.PublicAPI;

/**
 * Contains objects useful to {@link FunctionDefinition} instances
 * during expression compilation.
 */
@PublicAPI
public class FunctionCompilationContext extends AbstractFunctionContext {

  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";

  /**
   * The name under which an instance of {@link PortfolioStructure} should be bound.
   */
  public static final String PORTFOLIO_STRUCTURE_NAME = "portfolioStructure";

  /**
   * Creates an empty function compilation context.
   */
  public FunctionCompilationContext() {
  }

  /**
   * Creates a function compilation context as a deep copy of an existing one.
   * 
   * @param copyFrom context to copy elements from, not {@code null}
   */
  protected FunctionCompilationContext(final FunctionCompilationContext copyFrom) {
    super(copyFrom);
  }

  /**
   * Gets the source of securities.
   * @return the source of securities
   */
  public SecuritySource getSecuritySource() {
    return (SecuritySource) get(SECURITY_SOURCE_NAME);
  }

  /**
   * Sets the source of securities.
   * @param securitySource  the source of securities
   */
  public void setSecuritySource(SecuritySource securitySource) {
    put(SECURITY_SOURCE_NAME, securitySource);
  }

  /**
   * Gets the source of portfolio structure information.
   * @return the {@link PortfolioStructure} instance
   */
  public PortfolioStructure getPortfolioStructure() {
    return (PortfolioStructure) get(PORTFOLIO_STRUCTURE_NAME);
  }

  /**
   * Sets the source of portfolio structure information.
   * @param portfolioStructure the {@link PortfolioStructure} instance
   */
  public void setPortfolioStructure(final PortfolioStructure portfolioStructure) {
    put(PORTFOLIO_STRUCTURE_NAME, portfolioStructure);
  }

  @Override
  public FunctionCompilationContext clone() {
    return new FunctionCompilationContext(this);
  }

}
