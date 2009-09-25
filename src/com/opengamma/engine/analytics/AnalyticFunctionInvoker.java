/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * The interface through which an Analytic Function can actually be invoked.
 *
 * @author kirk
 */
public interface AnalyticFunctionInvoker {

  // Execution needs to have an ExecutionContext, which should have at the very least:
  // - The live data snapshot
  // - The ExecutorService
  // - Other stuff as we add them.
  
  Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Position position);

  Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Security security);
}
