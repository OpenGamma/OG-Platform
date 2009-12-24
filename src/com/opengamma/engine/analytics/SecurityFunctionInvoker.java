/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.security.Security;

/**
 * An analytic function invoker for a Security-specific analytic function. 
 *
 * @author kirk
 */
public interface SecurityFunctionInvoker
extends FunctionInvoker {

  Collection<ComputedValue<?>> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      Security security);
}
