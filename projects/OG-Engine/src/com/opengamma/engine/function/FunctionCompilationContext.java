/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.security.SecuritySource;
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
   * The name under which an instance of {@link ParentNodeResolver} should be bound.
   */
  public static final String PARENT_NODE_RESOLVER_NAME = "parentNodeResolver";

  public FunctionCompilationContext() {
  }

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
   * @return the {@link ParentNodeResolver} instance
   */
  public ParentNodeResolver getParentNodeResolver() {
    return (ParentNodeResolver) get(PARENT_NODE_RESOLVER_NAME);
  }

  /**
   * Sets the source of portfolio structure information.
   * @param parentNodeResolver the {@link ParentNodeResolver} instance
   */
  public void setParentNodeResolver(final ParentNodeResolver parentNodeResolver) {
    put(PARENT_NODE_RESOLVER_NAME, parentNodeResolver);
  }

  @Override
  public FunctionCompilationContext clone() {
    return new FunctionCompilationContext(this);
  }

}
