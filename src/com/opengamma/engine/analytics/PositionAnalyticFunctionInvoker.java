/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.position.Position;

/**
 * An analytic function invoker for a Security-specific analytic function. 
 *
 * @author kirk
 */
public interface PositionAnalyticFunctionInvoker
extends AnalyticFunctionInvoker {

  Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext,
      AnalyticFunctionInputs inputs,
      Position position);
}
