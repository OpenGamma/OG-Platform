/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.security.SecuritySource;


/**
 * Contains objects useful to {@link FunctionDefinition} instances
 * during expression compilation.
 *
 * @author kirk
 */
public class FunctionCompilationContext extends AbstractFunctionContext {
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_MASTER_NAME = "securityMaster";
  
  public void setSecurityMaster(SecuritySource secMaster) {
    put(SECURITY_MASTER_NAME, secMaster);
  }
  
  public SecuritySource getSecurityMaster() {
    return (SecuritySource) get(SECURITY_MASTER_NAME);
  }
  
}
