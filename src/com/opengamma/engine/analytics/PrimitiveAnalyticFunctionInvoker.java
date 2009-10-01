/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

/**
 * An invocation object for an analytic function whose result does not
 * depend on a position or a security.
 * Examples of these would be low-level analytic object generation functions,
 * like discount curve bootstrappers.
 *
 * @author kirk
 */
public interface PrimitiveAnalyticFunctionInvoker
extends AnalyticFunctionInvoker {

  Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext,
      AnalyticFunctionInputs inputs);
}
